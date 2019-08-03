(ns scan-img.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET POST context routes defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :as ring-response]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.edn :refer [wrap-edn-params]]            
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]))


(defn ok-resp
  [data]
  (-> (ring-response/response (pr-str data))
      (ring-response/content-type "application/edn")))


;;--------------------------------------------------------------
;; Process an upload for scanning
;;--------------------------------------------------------------
(defn process-scan-upload
  [params]
  (let [file (get params "file")
        temp-file (:tempfile file)
        file-name (:filename file)
        file-size (:size file)
        resp-data (dissoc file :tempfile)]
    (println (str "::-> params: " params))   
    (println (str "::-> file: "    file))
    (println (str "::-> tmp-file:"  temp-file))
    (println (str "::-> file name:" file-name))
    (println (str "::-> file size:" file-size))

    (do
      (let [target (io/file "resources" "public" "uploads" file-name) ]
        (io/copy temp-file target)
        (-> resp-data
            (assoc :message (str  "File [" file-name "] saved"))
            (assoc :path (.getPath target)) 
            (ok-resp))))))

;;--------------------------------------------------------------
;; Progress functions for multipart warapper. Called during uploads.
;;--------------------------------------------------------------
(defn progress-fn
  [request bytes-read content-length item-count]
  (println (str  "::> request " request ))
  (println (str  "::> content lenght " content-length ))
  (println (str  "::> item-count " item-count ))
  (println (str  "::> bytes-read " bytes-read )))


;;--------------------------------------------------------------
;; web site routes for handling web site
;;--------------------------------------------------------------
(defroutes site-handler
  (GET "/" [] (ring-response/resource-response "index.html" {:root "public"}))
  (resources "/"))


;;--------------------------------------------------------------
;; upload routes for handling payload
;;--------------------------------------------------------------
(defroutes upload-handler
  (-> (POST "/upload/scan" {params :params} (process-scan-upload params))
      (wrap-edn-params)
      (wrap-params)
      (wrap-multipart-params  {:progress-fn progress-fn})))


;;--------------------------------------------------------------
;; Appliction routes combining site nd upload routes
;;--------------------------------------------------------------
(def handler
  (routes site-handler
          upload-handler))


;;--------------------------------------------------------------
;; Refs for referenced by ring adapter as handler for
;; incomming requests
;;--------------------------------------------------------------
(def dev-handler (-> #'handler wrap-reload))

