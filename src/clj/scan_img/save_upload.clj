(ns scan-img.save-upload
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.core.async :as async]
            [clj-commons-exec :as exec]))


;;(def event-channel (async/chan (async/sliding-buffer 10)))

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
      (io/make-parents can-path))))

;;--------------------------------------------------------------
;; consumer for file processing
;;--------------------------------------------------------------
(defn save-file
  "Save file to resources/public/uploads. "
  [src file-name]
  (let [target (io/file "resources" "public" "uploads" file-name) ]
    (ensure-parent-dir! target)
    (io/copy src target)))

;;--------------------------------------------------------------
;; consumer for file processing
;;--------------------------------------------------------------
(defn file-consumer
  "Accepts file events though an channel and processes
  them asnynchrously"
  []
  (let [in (async/chan (async/sliding-buffer 64))]
    (async/go-loop [data (async/<! in)]
      (when data
        (save-file (:src data) (:file-name data))
        (recur (async/<! in))))
    in))


;;--------------------------------------------------------------
;; Producer for file processing
;;--------------------------------------------------------------
(defn file-producer
  "producer for file events delates
  to consumer channel"
  [src name consumer-ch]
  (async/go
    (async/>! consumer-ch {:src src :file-name name})))


;;--------------------------------------------------------------
;; Execute docker command
;; side effects!!!
;;--------------------------------------------------------------
(defn run-docker-version-command!
  "Run the docker version command and return relults"
  []
  (let [result @(exec/sh ["java" "-version"])]
    (if (some? (:out result))
      (assoc result :outstrlst (str/split (:out result) #"\n"))
      result)))

