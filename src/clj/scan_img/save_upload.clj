(ns scan-img.save-upload
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.core.async :as async]
            [clj-commons-exec :as exec]))



;;--------------------------------------------------------------
;; Execute docker command
;; side effects!!!
;;
;; TODO: use channel as input
;;--------------------------------------------------------------
(defn run-docker-version-command!
  "Run the docker version command and return relults"
  []
  (let [result @(exec/sh ["java" "-version"])]
    (if (some? (:out result))
      (assoc result :outstrlst (str/split (:out result) #"\n"))
      result)))


;;--------------------------------------------------------------
;; Process an upload for scanning
;; side effects!!!
;;--------------------------------------------------------------
(defn- ensure-parent-dir!
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
  them asnynchronously"
  []
  (let [in (async/chan (async/sliding-buffer 10))]
    (async/go-loop [data (async/<! in)]
      (when data
        (save-file (:src data) (:file-name data))
        (recur (async/<! in))))
    in))


(defn- fileupload-consumer2
  "Accepts file events though an channel and processes
  them asnynchronously"
  [in]
  (let [out (async/chan (async/chan))]
    (async/go-loop [data (async/<! in)]
      (when data
        (async/>! out (save-file (:src data) (:file-name data)))
        (recur (async/<! in))))
    out))





;;--------------------------------------------------------------
;; Consumer for running commands 
;;--------------------------------------------------------------
(defn- commandexec-consumer
  "Accepts command executiion events though an channel and processes
  them asnynchronously"
  [in]
  (let [out (async/chan)]
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
(defonce input-ch (fileupload-consumer))



;;--------------------------------------------------------------
;; Kicks off file processing as soon as ring handler has recieved
;; upload
;;--------------------------------------------------------------
(defn reg-upload-event
  "Register an event on the fileupload channel to signal that
  a file was uploaded. The consumer on the channel will take
  approprient action"
  [file-src file-name]
  (async/go
    (async/>! input-ch {:src file-src :file-name file-name})))


(defn reg-upload-event2
  "Register an event on the fileupload channel to signal that
  a file was uploaded. The consumer on the channel will take
  approprient action"
  [file-src file-name]
  (let [out-upload (file-upload-consumer2 input-ch)
        out-executor (commandexec-consumer out-upload)]
    (async/>!! in-ch {:src file-src :file-name file-name} callback)
    (<!! out-executor)))

;
