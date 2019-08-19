(ns scan-img.file-service
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [scan-img.utils :as utils]
            [clojure.core.async :as async]
            [clj-commons-exec :as exec]
            [taoensso.timbre :as timbre]
            [mount.core :as mount :refer [defstate]]))


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
;; Utility function for extracting results from
;; an exception
;;--------------------------------------------------------------
(defn exception->strlst
  "Given an exception object like
    {:exit -35545
     :exception #error {:cause \"error=2, No such file or directory\"
                        :via []  .... }}
    then will generate a list of strings"
  [e]
  (let [m (Throwable->map e)]
    (into [] (map str (:via m)))))

(defn remove-exception
  [result]
  (dissoc result :exception ))


;;--------------------------------------------------------------
;; Utility functions for extracting results from
;; a command execution
;;--------------------------------------------------------------
(defn execresult->strlist
  "Converts clj-commons-exec results to a list of strings
  The expcted result map looks as follows in case an
  exception happened:
  
  {:exit -559038737,
   :out nil,
   :err nil,
   :exception #error ...}"
  [result]
  (timbre/info "::->  execresult->strlist: " result)
  (cond
    (nil? result) (assoc {} :outstrlst ["Unknown executaion error!"
                                        "Examine server logs e.g. figwheel_server.log"])
    (some? (:out result)) (assoc result :outstrlst (str/split (:out result) #"\n"))
    (some? (:err result)) (assoc result :outstrlst (str/split (:err result) #"\n"))
    (some? (:exception result)) (assoc (dissoc result :exception)
                                       :outstrlst
                                       (exception->strlst (:exception result)))))


;;--------------------------------------------------------------
;; Executes the docker command. 
;; side effects!!!
;;--------------------------------------------------------------
(defn run-command!
  "Run the docker version command and return relults
  shell command expected to be provided in config.edn
  with the key :executable-cmd.

  Example:
  {:name \"Docker Image Scanner\"
   :executable-cmd [\"docker\" \"version\"]}:
  "g
  [data]
  (let [config (utils/read-config)
        command (:executable-cmd config)        
        result @(exec/sh command {:shutdown true})]
    (timbre/info "::==> run-command! results: " result)
    (execresult->strlist result)))


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
        (let [cmd-output (run-command! (assoc data :cannonical-path path))
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

