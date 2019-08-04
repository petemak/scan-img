(ns scan-img.save-upload
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]))


;;--------------------------------------------------------------
;; Process an upload for scanning
;; side effects!!!
;;--------------------------------------------------------------
(defn ensure-parent-dir!
  "Check and create parent directory
  -> seems make-parents fails gracefully returning false!!!!"
  [file]
  (let [parent-dir (io/as-file (.getParent file))]
    (if (not (.isDirectory parent-dir))
      (io/make-parents parent-dir))))

;;--------------------------------------------------------------
;; consumer for file processing
;;--------------------------------------------------------------
(defn save-file
  "Save file"
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
        (println "Event data: " data)
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
