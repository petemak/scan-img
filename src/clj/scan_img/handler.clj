(ns scan-img.handler
  (:require [scan-img.file-service :as fp]
            [compojure.core :refer [GET POST context routes defroutes]]
            [compojure.route :refer [resources]]
            [taoensso.timbre :as timbre]
            [clojure.core.async :as async]
            [ring.util.response :as ring-response]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.edn :refer [wrap-edn-params]]            
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]))

(timbre/set-level! :debug)

(defn ok-resp
  "Generate response map with specified data in the body"
  [data]
  (-> (ring-response/response (pr-str data))
      (ring-response/content-type "application/edn")))


;;--------------------------------------------------------------
;; Process an upload for scanning
;; side effects
;;--------------------------------------------------------------
(defn process-scan-upload
  "Handler processes file uploads
   First saves and then posts uploaded event on processing quest"
  [params]
  (let [file (get params "file")
        file-data (:tempfile file)
        file-name (:filename file)
        file-size (:size file)
        resp-data (dissoc file :tempfile)]
    (timbre/info "::-> calling FS with file : " file-data)    
    (let [results (fp/reg-upload-event file-data file-name)]
      (timbre/info "::-> reuslts from FS: " results)
      (-> resp-data
          (assoc :message (str  "File [" file-name "] saved"))
          (assoc :cmd-results results)
          (assoc :size file-size)
          (assoc :path "-")
          (ok-resp)))))


;;--------------------------------------------------------------
;; Progress functions for multipart warapper. Called during uploads.
;;--------------------------------------------------------------
(defn progress-fn
  "Progress function inoked by multi-part upload wrapper
   Could this be used to send SSE events to client?"
  [request bytes-read content-length item-count]
  (timbre/info "::> request " request)
  (timbre/info "::> content lenght " content-length)
  (timbre/info "::> item count " item-count)
  (timbre/info "::> bytes read " bytes-read))


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
