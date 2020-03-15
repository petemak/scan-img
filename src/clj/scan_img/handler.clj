(ns scan-img.handler
  (:require [buddy.auth :as auth]
            [scan-img.sec :as sec]
            [scan-img.file-service :as fp]
            [compojure.core :refer [GET POST context routes defroutes]]
            [compojure.route :refer [resources]]
            [taoensso.timbre :as timbre]
            [clojure.core.async :as async]
            [buddy.auth.middleware :as buddy-mid]
            [ring.util.response :as ring-response]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]))

(timbre/set-level! :debug)



(defn ok-resp
  "Generate response map with specified data in the body"
  [data]
  (-> (ring-response/response (pr-str data))
      (ring-response/content-type "application/edn")))


(defn show-login
  "Generate response map with specified data in the body"
  [request]
  (-> (ring-response/response {:login "REQUIRED"})
      (ring-response/content-type "application/edn")))



;;--------------------------------------------------------------
;; Authentication function
;; s
;;--------------------------------------------------------------
(defn do-login
  "Generate response map with specified data in the body"
  [request]
  (timbre/info "::--> handler/do-login - params: " (dissoc (:params request) :password))
  (let [[authenticated? results] (sec/login-user (:params request))]
    (timbre/info "::--> handler/do-login - authentication results: " authenticated? " - " results)
    (if authenticated?
      (-> (ring-response/redirect "/")
          (assoc :session {:token-pair results}))      
      (ring-response/redirect "/login"))))


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



(defn sec-process-file
  "Only run if authenticated"
  [req]
  (println (str "::--> hadler/sec-process-file" req))

  (if-not (auth/authenticated? req)
    (auth/throw-unauthorized)
    (process-file req)))


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
;; Process request for config file
;;--------------------------------------------------------------
(defn read-config
  "Handler loads config file"
  [params]
  (timbre/info "::--> handler/read-config - params: " params)
    
  (let [results (fp/sync-read-config)]
    (timbre/info "::--> handler/process-config - results from file service: " (:config (first (:results results))))
      (-> results
          (assoc :message (if (some? (:config (first (:results results)))) "Config file loaded" "config file not found!"))
          (ok-resp)))  )


;;--------------------------------------------------------------
;; Process an config file upload
;; side effects
;;--------------------------------------------------------------
(defn process-config
  "Handler processes config file uploads
   First saves"
  [params]
  (let [config (get params "config")]
    (timbre/info "::--> handler/process-config - params: " params)
    
    (let [results (fp/sync-save-config config)]
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
(defroutes public-routes
  (GET  "/login" request (show-login request))  
  (POST "/login" request (do-login request))
  (resources "/"))


;;--------------------------------------------------------------
;; Routes worth securing: handling payload
;;
;; NOTE: we are destructuring using :params which is filled in
;;       by the wrap-params-middleware.
;;       :params combines both :query-params and :form-params
;;--------------------------------------------------------------
(defroutes secured-routes
  (GET "/" [] (ring-response/resource-response "index.html" {:root "public"}))  
  (POST "/upload/scan" request (sec-process-file request))
  (POST "/upload/code" {params :params} (process-code params))
  (POST "/upload/config" {params :params} (process-config params))
  (GET "/download/config" {params :params } (read-config params)))

;;--------------------------------------------------------------
;; Appliction routes combining site nd upload routes
;;
;; Middleware:
;;------------
;;1) wrap-params - parse URL-encoded parameters and add following
;;                  keys to the request map
;;      - :query-params - A map of parameters from the query string
;;      - :form-params - A map of parameters from submitted form data
;;      - :params - A merged map of all parameters
;;2) wrap-edns-param - parse request body as edn and augment :params
;;
;;3) wrap-keyword-params - converts string keys in the :params map to keywords.
;;
;;
;; Ring request:  "/search&q=clojure"
;; ----------------------------------
;; {:request-method :get
;;  :uri "/search"
;;  :query-string "q=clojure"
;;  :query-params {"q" "clojure"}
;;  :form-params {}
;;  :params {"q" "clojure"}}
;;
;;encryption key must come from config file!!!
;; 
;;--------------------------------------------------------------
(defroutes app-routes
  (-> public-routes
      (sec/wrap-token)
      (wrap-edn-params))
  (-> secured-routes
      (sec/wrap-authenticate-user)
      (sec/wrap-token)
      (wrap-edn-params)
      (wrap-multipart-params {:progress-fn progress-fn})
      (sec/wrap-auth-cookie "cokiepasswd12345")))


;;--------------------------------------------------------------
;; Appliction routes combining site nd upload routes
;;--------------------------------------------------------------
(def handler
  (-> app-routes
      (wrap-keyword-params)
      (wrap-params)))


;;--------------------------------------------------------------
;; Refs for referenced by ring adapter as handler for
;; incomming requests
;;--------------------------------------------------------------
(def dev-handler (-> #'handler
                     wrap-reload))

