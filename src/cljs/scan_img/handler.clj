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
(defn process-file
  "Handler processes file uploads
   First saves and then posts uploaded event on processing quest"
  [req]
  (let [params (:params req)
        file (get params "file")
        file-data (:tempfile file)
        file-name (:filename file)
        file-size (:size file)
        file-type (get params "upload-type")
        user-name (:user-name params)
        password (:password params)
        resp-data (dissoc file :tempfile)]
    (timbre/info "::--> process-file upload - params: " params)
    (timbre/info "::--> process-file upload - :upload-type : " file-type)
    ;;
    (let [results (fp/sync-reg-image-event file-data file-name file-type user-name password)]
      (timbre/info "::--> process-file upload - reuslts from file service: " results)
      (-> resp-data
          (assoc :message (str  "File [" file-name "] saved"))
          (assoc :cmd-results results)
          (assoc :size file-size)
          (assoc :path (:cannonical-path results))
          (ok-resp)))))



;;--------------------------------------------------------------
;; Process an upload for scanning
;; side effects
;;--------------------------------------------------------------
(defn process-code
  "Handler processes file uploads
   First saves and then posts uploaded event on processing quest"
  [params]
  (let [code (:code params)
        user-name (:name params)
        password (:password params)]
    (timbre/info "::--> process-code - params: " params)
    
    (let [results (fp/sync-reg-code-event code user-name password)]
      (timbre/info "::--> process-code - reuslts from file service: " results)
      (-> results
          (assoc :message "Processing done")
          ;;(assoc :cmd-results results)
          ;;(assoc :size ??)
          (assoc :path (:cannonical-path results))
          (ok-resp)))))

;;--------------------------------------------------------------
;; Process an config file upload
;; side effects
;;--------------------------------------------------------------
(defn process-config
  "Handler processes config file uploads
   First saves"
  [params]
  (let [code (:config params)]
    (timbre/info "::--> handler/process-config - params: " params)
    
    (let [results {:results  [{:command ["save" "config.edn"]
                               :message "Config file saved"}]}]
      (timbre/info "::--> handler/process-config - results from file service: " results)
      (-> results
          (assoc :message "Processing done")
          (assoc :path (:cannonical-path results))
          (ok-resp)))))

;;--------------------------------------------------------------
;; Progress functions for multipart warapper. Called during uploads.
;;--------------------------------------------------------------
(defn progress-fn
  "Progress function inoked by multi-part upload wrapper
   Could this be used to send SSE events to client?"
  [request bytes-read content-length item-count]
  (timbre/info "::==> handler/progress-fn request " request)
  (timbre/info "::==> handler/progress-fn content lenght " content-length)
  (timbre/info "::==> handler/progress-fn item count " item-count)
  (timbre/info "::==> handler/progress-fn bytes read " bytes-read))


;;--------------------------------------------------------------
;; web site routes for handling web site
;;--------------------------------------------------------------
(defroutes site-routes
  (GET "/" [] (ring-response/resource-response "index.html" {:root "public"}))
  (resources "/"))


;;--------------------------------------------------------------
;; upload routes for handling payload
;;
;; NOTE: we are destructuring using :params which is filled in
;;       by the wrap-params-middleware.
;;       :params combines both :query-params and :form-params
;;--------------------------------------------------------------
(defroutes upload-routes
  (POST "/upload/scan" request (process-file request))
  (POST "/upload/code" {params :params} (process-code params))
  (POST "/upload/config" {params :params} (process-config params)))

;;--------------------------------------------------------------
;; Appliction routes combining site nd upload routes
;;--------------------------------------------------------------
(def wrapped-upload-routes
  (-> upload-routes
      (wrap-edn-params)
      (wrap-multipart-params {:progress-fn progress-fn})
      (wrap-params)))


;;--------------------------------------------------------------
;; Appliction routes combining site nd upload routes
;;--------------------------------------------------------------
(def handler
  (routes site-routes
          wrapped-upload-routes))


;;--------------------------------------------------------------
;; Refs for referenced by ring adapter as handler for
;; incomming requests
;;--------------------------------------------------------------
(def dev-handler (-> #'handler wrap-reload))
