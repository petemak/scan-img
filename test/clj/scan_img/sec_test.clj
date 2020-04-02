(ns scan-img.sec-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [scan-img.user :as user]
            [scan-img.sec :as sec]
            [scan-img.db :as db]
            [clojure.string :as str]
            [ring.mock.request :as mock]))


(def login-req  (-> (mock/request :post "/login")
                    (mock/content-type "application/edn")
                    (mock/body "{:user-id  \"test\"  
                                 :password \"testpwd\" ")))


(def home-req (-> (mock/request :post "/")
                   (mock/content-type "application/edn")
                   (mock/body "{:user-id  \"test\"  
                                :password \"testpwd\" ")))

(def usr {:user-id "sec-user"
           :password "test-password"})



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


(defn test-handler
  [req]
  {:status 200
   :header {}
   :body "results"})


(deftest login-user
  (testing  "Testing login-user"
    (testing "that an exisiting user is correctly itdenified"
      (is (= true (sec/login-user usr))))))

(deftest wrap-authenticated-user
  (testing "authentication middleware"
    (testing "unauthenticated user gets a 302 "
      (let [hdlr (sec/wrap-authenticate-user test-handler )
            resp (hdlr home-req)]
        (is (= 302 (:status resp)))))
    (testing "unauthenticated user get a header with redirect"
      (let [hdlr (sec/wrap-authenticate-user test-handler )
            resp (hdlr home-req)]
        (is (not= nil (:headers resp)))
        (is (not= nil (-> resp :headers (get "Location"))))
        (is (= 0 (str/index-of (-> resp :headers (get "Location")) "/")))
        (is (= true (str/includes? (-> resp :headers (get "Location")) "/login?m=")))))))
