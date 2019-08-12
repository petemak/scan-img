(ns scan-img.save-upload
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.core.async :as async]
            [clj-commons-exec :as exec]
            [mount.core :as mount :refer [defstate]]))





;;--------------------------------------------------------------
;; Execute docker command
;; side effects!!!
;;
;; TODO: use channel as input
;;--------------------------------------------------------------
(defn- run-docker-version-command!
  "Run the docker version command and return relults"
  []
  (let [result @(exec/sh ["java" "-version"])]
    (if (some? (:out result))
      (assoc result :outstrlst (str/split (:out result) #"\n"))
      result)))




;;--------------------------------------------------------------
;; Execute docker command
;; side effects!!!
;;--------------------------------------------------------------
(defn run-command!
  "Run the docker version command and return relults"
  [data]
  (let [result @(exec/sh ["docker" "version"])]
    (if (some? (:out result))
      (assoc result :outstrlst (str/split (:out result) #"\n"))
      (assoc result :outstrlst (Throwable->map (:exception result))))))




;;--------------------------------------------------------------
;; Process an upload for scanning
;; side effects!!!
;;--------------------------------------------------------------
(defn ensure-parent-dir!
  "Check and create parent directory if it doesnt exist.
  Uses canonical path to remove OS dependent path strings.
  -> seems make-parents fails gracefully returning false
  in case of error !!!!"
  [f]
  (let [can-path (.getCanonicalPath f)
        parent-file (io/as-file (.getParent f))]
    
    (if (not (.isDirectory parent-file))
      (io/make-parents can-path))
    can-path))

;;--------------------------------------------------------------
;; consumer for file processing
;;--------------------------------------------------------------
(defn- save-file
  "Save file to resources/public/uploads. "
  [src file-name]
  (let [target (io/file "resources" "public" "uploads" file-name)
        can-path (ensure-parent-dir! target)]   
    (io/copy src target)
    can-path))


;;(def event-channel (async/chan (async/sliding-buffer 10)))
;;--------------------------------------------------------------
;; Consumer for file processing
;;--------------------------------------------------------------
(defn- fileupload-consumer
  "Accepts file events though an channel and processes
  them asnynchronously
  input channed expected to contain a map
  :file-data - file data
  :file-name - name of file"
  [in]
  (let [out (async/chan 10)]
    (async/go-loop [data (async/<! in)]
      (when data
        (if-let [path (save-file (:file-data data) (:file-name data))]
          (async/>! out))
        (recur (async/<! in))))
    out))





;;--------------------------------------------------------------
;; Consumer for running commands 
;;--------------------------------------------------------------
(defn- commandexec-consumer
  "Accepts command executiion events though an channel and processes
  them asnynchronously"
  [in]
  (let [out (async/chan 10)]
    (async/go-loop [data (async/<! in)]
      (when data
        (async/>! out (run-command! data))
        (recur (async/<! in))))
    out))





;;--------------------------------------------------------------
;; Channel for file uploads
;;
;; TODO: use component to manager lifecycle create and de
;;--------------------------------------------------------------
(defn assemble-pipeline
  "Assembles the chain of processors connected using
  channels and returns a map with 3 keys for the channels
  :input-chan, intermediate-chan and :output-chan"
  []
  (let [input-chan (async/chan 10)
        intermediate-chan (fileupload-consumer input-chan)
        output-chan (commandexec-consumer intermediate-chan)]
    {:input-chan input-chan
     :intermediate-chan intermediate-chan
     :output-chan output-chan}))


(defn close-pipeline
  "Close channels"
  [chans]
  (map async/close! (vals chans)))



;;--------------------------------------------------------------
;; Execute docker command
;;--------------------------------------------------------------
(defstate processing-pipeline
  :start (assemble-pipeline)
  :stop (close-pipeline processing-pipeline))






