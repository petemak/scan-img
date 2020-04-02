(ns scan-img.user-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as client]
            [mount.core :as mount]
            [scan-img.db :as db]
            [scan-img.sec :as sec]
            [scan-img.user :as user]))



(def test-user {:user-id "test123" :password "testpwd"})



(defn setup-db
  []
  (mount/start)
  (user/register-user test-user))

(defn shutdown-db
  []
  (user/unregister-user test-user)
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
      (is (= true (first (user/authenticate-user test-user)))))))


(deftest retract-user
  (testing "User retract user"
    (let [user {:user-id "testuser1"
                :password "testpassword"}]
      (testing "registration of a user"
        (let [res (user/register-user user)]
          (is (= "testuser1" (:user-id (user/load-user user))))))
      (testing "retraction of a user"
        (let [res (user/unregister-user user)]
          (is (= nil (user/load-user user))))))))





(comment
  (require :reload '[scan-img.user :as u])
  (require '[mount.core :as mount])

  (def user1 {:user-id "user1"
              :password "pwd1"})

  (mount/start)

  (def dbusr (u/load-user user1))
  )
