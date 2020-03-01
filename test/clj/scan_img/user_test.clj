(ns scan-img.user-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as client]
            [mount.core :as mount]
            [scan-img.db :as db]
            [scan-img.sec :as sec]
            [scan-img.user :as user]))



(def test-user {:user-id "test" :password "testpwd"})


(defn store-user
  []
  (user/register-user test-user))


(defn setup-db
  []
  (mount/start)
  (store-user))

(defn shutdown-db
  []
  (mount/stop))



(defn user-fixture
 [f]
  (setup-db)
  (f)
  (shutdown-db))



(use-fixtures :once user-fixture)

(deftest test-auth
  (testing "authentication"
    (testing "user not nil"
      (is (not= nil (user/load-user test-user))))
    (testing "user id is same"
      (is (= (:user-id test-user) (:user-id (user/load-user test-user)))))
    (testing "password credentials"
      (= true (first (user/authenticate-user test-user))))))
