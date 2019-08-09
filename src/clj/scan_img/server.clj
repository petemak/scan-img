(ns scan-img.server
  (:require [clojure.edn :as edn]
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
(defn load-cfg
  "Load appliction configuration"
  [file-name]
  (edn/read-string (slurp file-name)))



;; -------------------------------------------------------------
;; Reads the application configuration file
;;--------------------------------------------------------------
(def cfg (load-cfg "config.edn"))



;; -------------------------------------------------------------
;; From docs: :timeout is optional, when no timeout,
;; stop immediately
;; ------------------------------------------------------------- 
(defn stop-httpkit
  "Stop server gracefully (wait 100ms) to allow for existing
  requests to terminate"
  []
  (when-not (nil? @httpkit-server)
    (@httpkit-server :timeout 100)))


;; -------------------------------------------------------------
;; Reads the application configuration file
;;--------------------------------------------------------------
(defn -main [& args]
  (let [port (or (:port cfg) "3000")
        srv (or (:server cfg) :httpkit)]
    (println "Starting '" srv "' server on port '" port "' ....")
    (if (= srv :jetty)
      (jetty/run-jetty handler/dev-handler {:port port :join? false})
      (reset! httpkit-server (httpkit/run-server handler/dev-handler {:port port})))))
