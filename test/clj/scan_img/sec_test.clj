(ns scan-img.sec-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [scan-img.user :as user]
            [scan-img.sec :as sec]))


(def login-req (-> (mock/request :post "/login")
                   (mock/content-type "application/edn")
                   (mock/body "{:user-id  \"test\"  
                                :password \"testpwd\" ")))

(def user {:user-id "sec-user"
           :passord "test-password"})



(defn init-db
  []
  (mount/start)
  (user/register-user user))


(defn stop-db
  []
  (mount/stop))

(defn db-setup [f]
  (init-db)
  (f)
  (stop-db))


(use-fixtures :once db-setup)


(deftest login-user
  (testing  "Testing login-user"
    (testing "that an exisiting user is correctly itdenified"
      (is (= true (sec/login-user user))))))
