(ns scan-img.file-service
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [scan-img.utils :as utils]
            [clojure.core.async :as async]
            [clj-commons-exec :as exec]
            [mount.core :as mount :refer [defstate]]))

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
  "
  [data]
  (let [command (:executable-cmd (utils/edn-from-home))
        result @(exec/sh command {:shutdown true})]
    (if (some? (:out result))
      (assoc result :outstrlst (str/split (:out result) #"\n"))
      (assoc {} :outstrlst (map str (Throwable->map (:exception result)))))))


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

