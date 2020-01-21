(ns scan-img.file-service
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [scan-img.utils :as utils]
            [clojure.core.async :as async]
            [clj-commons-exec :as exec]
            [taoensso.timbre :as timbre]
            [chord.http-kit :refer [with-channel]]))


(timbre/set-level! :debug)

;;--------------------------------------------------------------
;; Atom holds in and output channels for file service
;;--------------------------------------------------------------
(def processor (atom nil))



;;--------------------------------------------------------------
;; consumer for file processing
;;--------------------------------------------------------------
(defn save-file!
  "Save file to resources/public/uploads. "
  [data]
  (timbre/info "::==> file-service/save-file!: name of data to save " (:file-name data) "... ")
  (let [unique-name (utils/unique-str (:file-name data))
        docker-txt? (= :docker-text (:file-type data))
        file-name   (if docker-txt? "dockerfile" unique-name) 
        target      (io/file "resources" "public" "uploads" unique-name file-name)
        can-path    (utils/ensure-parent-dir! target)
        ret         {:cannonical-path can-path
                     :context-directory (utils/parent-dir can-path)
                     :file-name file-name}]
    (if docker-txt?
      (spit target  (:file-data data))
      (io/copy (:file-data data) target))
    ret))



;;--------------------------------------------------------------
;; Replace the palce holders with actual values
;; side effects!!!
;;--------------------------------------------------------------
(defn process-command-params
  "Given a string list with place-holders
  [[\"docker\" \"load {{cannonical-path}}\"]
   [\"docker\" \"run --rm {{image-name}}\"], replace these with values in the
  context map {:cannonic-path \"/uploads/image.pkz\"}"  
  [commands data]
  (let [pf (partial utils/replace-placeholder-from-map data)]
    (map pf commands)))


 

;;--------------------------------------------------------------
;; Which commands to execute?
;; a) Commands to process uploaded file -> commands in config.edn
;; b) Commands from uploaded command file -> commands.edn
;;--------------------------------------------------------------
(defn resolve-command-config!
  "Resolves the commands to execute from the file data:
  :file-data - actual file data
  :file-name - name of file
  :file-type - type of file
  :cannonical-path - cannonical path of file "
  [data]
  (let [type (:file-type data)]
    (cond
      (= type :docker-image) (utils/read-config)
      (= type :docker-text) (utils/read-config)
      (= type :config) (utils/read-config)
      (= type "command") (utils/edn-from-file (:file-data data))
      :else nil)))




;;--------------------------------------------------------------
;; Run commands. 
;; side effects!!!
;;--------------------------------------------------------------
(defn run-commands!
  "Run shell command and return relults. The shell
  commands expected to be provided in config.edn
  with the key :executable-cmd.
  Example:
  {:name \"Docker Image Scanner\"
   :executable-cmd [[\"command-1\" \"params-1\"]
                    [\"command-2\" \"params-2\"]]}

  The return map contains a list of maps mapped to the key :results
  Each map in the list contains tje executed command and results
  {:results [ {:command \"bla bla\"
               :message \"Message...\"
               :outstrlst \"exception...\"}
              {:command ...
               :message ...
               outstrlst ...}  ]}"
  [data]
  (timbre/info "::--> file-service/run-commands! - file name: " (:file-name data))
  (timbre/info "::--> file-service/run-commands! - cannonical path: " (:cannonical-path data))
  ;; (timbre/info "::--> file-service/run-commands! - input data: " data)
  (if-let [config (resolve-command-config! data)]
    (let [commands (process-command-params (:executable-cmd config) data)]
      (timbre/info "::==> file-service/run-commands! - commands processed for execution: " (pr-str commands))
      (timbre/info "::==> file-service/rund-command! with config found: " config)

      (try 
        (loop [cmds commands
               accum {:results []}] ;; fix this: how to ensure results are in a vector and not a list??
          (if (empty? cmds)
            accum
            (do
              (let [cmd (first cmds)
                    result @(exec/sh cmd {:shutdown true})]
                (timbre/info "::==> file-service/run-command! executed command --[" (pr-str cmd) "]--")
                ;;(timbre/info "::==> run-command! results --[ " result "... ]--")
                (recur (rest cmds) (utils/execresult->strlist cmd  result accum))))))
        (catch Exception e          
          (utils/cmd-error-msg data commands  {:results []}))))
    (do
      (timbre/info "::==> file-service/run-commands! - Command execution failed. Commands not found!")
      (utils/cmd-error-msg data nil {:results []}))))





;;--------------------------------------------------------------
;; Start consumer for file processing
;;--------------------------------------------------------------
(defn start-processor
  "Waits for file events on channel and processes
  them asnynchronously
  input channel expected to contain a map
  :file-data - file data
  :file-name - name of file
  returns a map of input and output channels"
  [in out]
  (async/go-loop [data (async/<! in)]
    (timbre/info "::==> file-service/start-processor - data from queue: " data)
    (when data
      (when-let [ret-map (save-file! data)]
        (let [cmd-output (run-commands! (merge data ret-map))
              results (merge cmd-output ret-map)]
          (async/>! out results)))
      (recur (async/<! in)))))


;;--------------------------------------------------------------
;; Stop consumer for file processing
;;--------------------------------------------------------------
(defn stop-processor
  "closes channels for in and output and empties the atom"
  []
  (swap! processor #(map async/close! (vals %)))
  (reset! processor nil))


;;--------------------------------------------------------------
;; Return channels for processing
;; Can only be :input-chan or :output-chan
;;--------------------------------------------------------------
(defn channel
  [chan-id]
  (if (or (nil? @processor)
          (nil? (get @processor chan-id))
          (empty? @processor))
    (let [in (async/chan 10)
          out (async/chan 10)]
      (start-processor in out)
      (reset! processor {:input-chan in
                         :output-chan out})))
  (get @processor chan-id))


;;--------------------------------------------------------------
;; Queue events defined by descriptor
;;--------------------------------------------------------------
(defn queue-event
  "Que event for processing"
  [evt-descr]
  (if (async/>!! (channel :input-chan) evt-descr)
    (async/<!! (channel :output-chan))))


;;--------------------------------------------------------------
;; Kicks off file processing as soon as ring handler has recieved
;; an image upload
;;--------------------------------------------------------------
(defn async-reg-image-event
  "Register an event on the fileupload channel to signal that
  a file was uploaded. Argumets are the file source, file name and type.
  The consumer on the channel will take
  approprient action"
  [file-src file-name file-type user-name password]
  (let [descr {:file-data file-src
               :file-name file-name
               :file-type :docker-image
               :user-name user-name
               :password password}]
    (queue-event descr)))



;;--------------------------------------------------------------
;; Kicks off file processing as soon as ring handler has recieved
;; an image upload
;;
;; For SSE see https://github.com/jarohen/chord
;;--------------------------------------------------------------
(defn sync-reg-image-event
  "Register an event on the fileupload channel to signal that
  a file was uploaded. Argumets are the file source, file name and type.
  The consumer on the channel will take
  approprient action"
  [image-file file-name file-type user-name password]
  (let [data {:file-data image-file
              :file-name file-name
              :file-type :docker-image
              :user-name user-name
              :password password}]

    (when-let [ret-map (save-file! data)]
      (let [cmd-output (run-commands! (merge data ret-map))]
        (merge cmd-output ret-map) ))) )





;;--------------------------------------------------------------
;; Kicks off file processing as soon as ring handler has recieved
;; code upload
;;--------------------------------------------------------------
(defn async-reg-code-event
  "Register a code upload event o the channel to signal that
  a code  was uploaded. Argumets are the file source, file name and type.
  The consumer on the channel will take
  approprient action"
  [code user-name password]
  (timbre/info "::--> reg-code-event: code size = " (count code) ", name = " user-name ", pwd = " password  
               (let [descr {:file-data code
                            :file-name "docker-file"
                            :file-type :docker-text
                            :user-name user-name
                            :user-password password}]
                 (queue-event descr))))


;;--------------------------------------------------------------
;; Kick off code processing as soon as ring handler has recieved
;; code upload
;;--------------------------------------------------------------
(defn sync-reg-code-event
  "Register a code upload event o the channel to signal that
  a code  was uploaded. Argumets are the file source, file name and type.
  The consumer on the channel will take
  approprient action"  
  [code user-name password]
  (timbre/info "::--> sync-reg-code-event: code size = " (count code) ", name = " user-name ", pwd = " password)  
  (let [data {:file-data code
              :file-name "docker-file"
              :file-type :docker-text
              :user-name user-name
              :user-password password}]    
    (when-let [ret-map (save-file! data)]
      (let [cmd-output (run-commands! (merge data ret-map))]
        (merge cmd-output ret-map) ))))



;;--------------------------------------------------------------
;; Save configuration file 
;;--------------------------------------------------------------
(defn sync-save-config
  "Save configuration provided as text"
  [config]
  (let[ret (utils/save-config config)
       results {:results  [{:command ["save" "config.edn"]
                            :message "Config file saved"
                            :outstrlst [ret]}]}]
    results))


;;--------------------------------------------------------------
;; Read configuration file 
;;--------------------------------------------------------------
(defn sync-read-config
  []
  (let[ret (utils/read-config)
       results {:results  [{:command ["Read" "config.edn"]
                            :message "Config file loaded"
                            :config ret}]}]
    results))
