(ns middleware-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as client]
            [scan-img.user :as user]
            [scan-img.sec :as sec]
            [mount.core :as mount]
            [ring.util.response :as ring-resp]))


(def usr {:user-id "sec-user"
           :password "test-password"})


(defn body-echo-handler
  [request]
  (if-let [body (:body request)]
    (-> (ring-resp/response body)
        (ring-resp/content-type "text/plain")
        (ring-resp/charset "utf-8"))
    (-> (ring-resp/response "No body submitted")
        (ring-resp/status 400))))

(defn init-db
  []
  (mount/start)
  (user/register-user usr))


(defn stop-db
  []
  (user/unregister-user usr)
  (mount/stop))

(defn db-setup [f]
  (init-db)
  (f)
  (stop-db))


(use-fixtures :once db-setup)



(def app (-> body-echo-handler
             (sec/wrap-authenticate-user)))



(deftest auth-test
  (testing "when a request is submitted"
    (testing "and the user is not anauthenticated then the response status must be 302"
      (let [response (app (mock/request :get "/"))]
        (is (= 302 (:status response)))))

    (testing "and the is anauthenticated then the reponse status must be 200 OK"
      (let [response (app (mock/request :get "/"))]
        (is (= 200 (:status response)))))))
