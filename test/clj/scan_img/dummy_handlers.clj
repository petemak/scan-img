(ns scan-img.dummy-handlers
  (:require [ring.util.response :as ring-resp]))

(defn body-echo-handler
  [request]
  (if-let [body (:body request)]
    (-> (ring-resp/response body)
        (ring-resp/content-type "text/plain")
        (ring-resp/charset "utf-8"))
    (-> (ring-resp/response "No body submitted")
        (ring-resp/status 400))))
