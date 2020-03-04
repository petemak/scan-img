(ns scan-img.server
  (:require [clojure.edn :as edn]
            [mount.core :as mount]
            [scan-img.utils :as utils]
            [scan-img.handler :as handler]
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
(defn stop-server
  "Stop server gracefully (wait 100ms) to allow for existing
  requests to terminate"
  []
  (when-not (nil? @httpkit-server)
    (@httpkit-server :timeout 100)))


(defn start-sever
  "Starts the server"
  []
  (let [port (or (:port cfg) "3000")
        srv (or (:server cfg) :httpkit)]
    (println "Starting '" srv "' server on port '" port "' ....")
    (mount/start)
    (if (= srv :jetty)
      (jetty/run-jetty handler/dev-handler {:port port :join? false})
      (reset! httpkit-server (httpkit/run-server handler/dev-handler {:port port}))))  )

;; -------------------------------------------------------------
;; Reads the application configuration file
;;--------------------------------------------------------------
(defn -main [& args]
  (mount/start)
  (start-sever))
