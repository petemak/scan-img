(ns scan-img.file-service
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [scan-img.utils :as utils]
            [clojure.core.async :as async]
            [clj-commons-exec :as exec]
            [taoensso.timbre :as timbre]))


(timbre/set-level! :debug)

;;--------------------------------------------------------------
;; Atom holds in and output channels for file service
;;--------------------------------------------------------------
(def processor (atom nil))



;;--------------------------------------------------------------
;; consumer for file processing
;;--------------------------------------------------------------
(defn save-file
  "Save file to resources/public/uploads. "
  [data]
  (let [unique-name (utils/unique-str (:file-name data))
        target (io/file "resources" "public" "uploads" unique-name)
        can-path (utils/ensure-parent-dir! target)
        docker-txt? (= :docker-txt (:file-type data))]
    (if docker-txt?
      (spit target  (:file-data data))
      (io/copy (:file-data data) target))
    can-path))




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
      (= type "image") (utils/read-config)
      (= type :docker-text) (utils/read-config)              
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
                    [\"command-2\" \"params-2\"]]}"
  [data]
  (timbre/info "::-> run-commands! - input data - :file-name " (:file-name data))
  (timbre/info "::-> run-commands! - input data - :file-type " (:file-type data))
  (timbre/info "::-> run-commands! - input data - :file-data " (:file-data data))
  (if-let [config (resolve-command-config! data)]
    (let [commands (process-command-params (:executable-cmd config) data)]
      (loop [cmds commands
             accum nil]
        (if (empty? cmds)
          accum
          (do
            (let [cmd (first cmds)
                  result @(exec/sh cmd {:shutdown true})]
              (timbre/info "::==> rund-command! with config found: " config)
              (timbre/info "::==> run-command! executed --[" cmd "]-- result: " result)
              (recur (rest cmds) (utils/execresult->strlist result accum)))))))
    (do
      (timbre/info "::-> run-commands! - Command execution failed. Commands not found!")
      (utils/execresult->strlist {:exit nil
                                  :out nil
                                  :err (str  "Command execution for failed!\n"
                                             "File name: " (:file-name data) "\n"
                                             "File type: " (:file-type data) "\n"
                                             "Commands: " (:file-type data) "\n"
                                             "Or if its a command file make sure 
                                              commands are specified.")
                                  :exception nil}
                                 nil))))


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
    (when data
      (when-let [path (save-file data)]
        (let [cmd-output (run-commands! (assoc data :cannonical-path path))
              results (assoc cmd-output :cannonical-path path)]
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
(defn reg-upload-event
  "Register an event on the fileupload channel to signal that
  a file was uploaded. Argumets are the file source, file name and type.
  The consumer on the channel will take
  approprient action"
  [file-src file-name file-type]
  (let [descr {:file-data file-src :file-name file-name :file-type file-type}]
    (queue-event descr)))


;;--------------------------------------------------------------
;; Kicks off file processing as soon as ring handler has recieved
;; code upload
;;--------------------------------------------------------------
(defn reg-code-event
  "Register an event on the fileupload channel to signal that
  a file was uploaded. Argumets are the file source, file name and type.
  The consumer on the channel will take
  approprient action"
  [code user-name password]
  (let [descr {:file-data code
               :file-name "docker-file"
               :file-type :docker-text
               :user-name user-name
               :user-password password}]
    (queue-event descr)))
