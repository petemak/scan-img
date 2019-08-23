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
(defn- save-file
  "Save file to resources/public/uploads. "
  [src file-name]
  (let [unique-name (utils/unique-str file-name)
        target (io/file "resources" "public" "uploads" unique-name)
        can-path (utils/ensure-parent-dir! target)]   
    (io/copy src target)
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
  (if-let [config (utils/read-config)]
    
    (let [commands (process-command-params (:executable-cmd config) data)]
      (loop [cmds commands
             accum nil]
        (if (empty? cmds)
          accum
          (do
            (let [cmd (first cmds)
                  result @(exec/sh cmd {:shutdown true})]
               (timbre/info "::==> run-command! executed --[" cmd "]-- result: " result)
               (timbre/info "::==> Config found: " (utils/read-config))                     
               (recur (rest cmds) (utils/execresult->strlist result accum)))))))
    (do
      (timbre/info "::==> command execution failed. config.edn not dound!")      
      (utils/execresult->strlist {:exit nil
                                  :out nil
                                  :err (str  "Command execution failed!\n"
                                       "Make sure \nconfig.edn\n is in the expected path")
                                  :exception nil}))))


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
      (when-let [path (save-file (:file-data data) (:file-name data))]
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
;; Kicks off file processing as soon as ring handler has recieved
;; upload
;;--------------------------------------------------------------
(defn reg-upload-event
  "Register an event on the fileupload channel to signal that
  a file was uploaded. The consumer on the channel will take
  approprient action"
  [file-src file-name]
  (if (async/>!! (channel :input-chan)
                  {:file-data file-src :file-name file-name})
    (async/<!! (channel :output-chan))))
