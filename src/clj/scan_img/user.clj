(ns scan-img.user
  (:require [buddy.hashers :as bhs]
            [scan-img.db :refer [storage]]))


(defn register-user
  "REgister a new users"
  [user]
  (let [db-user (.load-user storage user)]
    (when-not (= (:user-id user) (second (first db-user))) 
      (.save-user storage (assoc user :passoword (bhs/encrypt (:password user)))))))


(defn lod-user
  "Load the user using the used id"
  [user]
  (let [db-user (.load-user storage user)]
    (when-not (nil? db-user)
      (first db-user))))
