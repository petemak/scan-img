(ns scan-img.server
  (:require [clojure.edn :as edn]
            [scan-img.utils :as utils]
            [scan-img.handler :as handler]
            [mount.core :as mount]
            [config.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [org.httpkit.server :as httpkit])
  (:gen-class))



;; -------------------------------------------------------------
;; Reads the application configuration file
;;--------------------------------------------------------------
(defonce httpkit-server (atom nil))



;; -------------------------------------------------------------
;; Reads the application configuration file
;;--------------------------------------------------------------
(def cfg (utils/edn-from-home))


;; -------------------------------------------------------------
;; From docs: :timeout is optional, when no timeout,
;; stop immediately
;; ------------------------------------------------------------- 
(defn stop-httpkit
  "Stop server gracefully (wait 100ms) to allow for existing
  requests to terminate"
  []
  (when-not (nil? @httpkit-server)
 ;;   (mount/stop #'scan-img.file-service/file-processor)
    (@httpkit-server :timeout 100)))

;; -------------------------------------------------------------
;; Reads the application configuration file
;;--------------------------------------------------------------
(defn -main [& args]
  (let [port (or (:port cfg) "3000")
        srv (or (:server cfg) :httpkit)]
    (println "Starting '" srv "' server on port '" port "' ....")
;;    (mount/start #'scan-img.file-service/file-processor)
    (if (= srv :jetty)
      (jetty/run-jetty handler/dev-handler {:port port :join? false})
      (reset! httpkit-server (httpkit/run-server handler/dev-handler {:port port})))))
