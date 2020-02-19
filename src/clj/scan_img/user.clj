(ns scan-img.user
  (:require [buddy.hashers :as bhs]
            [scan-img.db :refer [storage]]))


(defn register-user
  "Register a new user. Password is encrypted before storage.
   Returns a map with user named entity-id and value
   {:entity-id 175xxxxxxxxxx :value ...}"
  [user]
  (let [db-user (.load-user storage user)]
    (when-not (= (:user-id user) (second (first db-user)))
      (let [res @(.save-user storage (assoc user :password (bhs/encrypt (:password user))))
            txd (second (:tx-data res))]
        {:entity-id (:e txd)
         :value (:v txd)}))))


(defn load-user
  "Load the user using the used id. 
   Datomic datoms [123133 \"user id\" \"password\"]"
  [user]
  (let [db-user (.load-user storage user)]
    (when-not (nil? db-user)
      (when-let [datom (first db-user)]
        {:id (first datom)
         :user-id (second datom)
         :password (last datom)}))))



;;----------------------------------------------------------------------
;; The authfn is responsible for the second step of authentication.
;; It receives the parsed auth data from request and should return a logical true
;;----------------------------------------------------------------------
(defn authenticate-user
  "uses the submitted user name to retrieve user credentials and
   compares with the submitted password provided by the user"
  [credentials]
  (println (str "::--> user/authenticate-user" credentials))
  
  (let [stored-user (load-user credentials)]
    (if (and stored-user (bhs/check (:password credentials) (:password stored-user)))
      [true (dissoc stored-user :password)]
      [false {:message "Invalid user name or password"}])))
