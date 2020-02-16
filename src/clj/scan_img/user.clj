(ns scan-img.user
  (:require [buddy.hashers :as bhs]
            [scan-img.db :refer [storage]]))


(defn register-user
  "Register a new user. Password is encrypted before storage"
  [user]
  (let [db-user (.load-user storage user)]
    (when-not (= (:user-id user) (second (first db-user))) 
      (.save-user storage (assoc user :password (bhs/encrypt (:password user)))))))


(defn load-user
  "Load the user using the used id
   Datomic datoms [123133 \"user id\" \"password\"]"
  [user]
  (let [db-user (.load-user storage user)]
    (when-not (nil? db-user)
      (let [datom (first db-user)]
        {:id (first datom)
         :user-id (second datom)
         :password (last datom)}))))
