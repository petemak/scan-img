(ns scan-img.db
  (:require [datomic.api :as d]
            [buddy.hashers :as bh]
            [scan-img.schema :as schema]
            [taoensso.timbre :as timbre]            
            [mount.core :refer [defstate]]))


;;------------------------------------------------------------------
;; User querry
;;------------------------------------------------------------------
(def user-query '[:find ?e ?id ?pwd
                  :in $ ?id
                  :where [?e :user/id ?id]
                         [?e :user/password ?pwd]])


;;------------------------------------------------------------------
;; User querry
;;------------------------------------------------------------------
(def token-query '[:find ?te ?val ?iat
                   :in $ ?uid
                   :where [?ue :user/id ?uid]
                          [?te :token/uref ?ue]
                          [?te :token/val ?val]
                          [?te :token/iat ?iat]])




;;------------------------------------------------------------------
;; Storage protocol defines how to store entities
;;------------------------------------------------------------------
(defprotocol Storage
  "Protocol for persisting data. Can be implemented with
   deftype or defrecord. The functions are polymorphic and will
   dipatch on the first (this) argument"

  (save-user [this user] "Load specified user credentials. 
                          Expected key :user-name and :secret")

  (load-user [this user] "Saves specified user credentials. 
                          Expected keys :user-name")

  (find-token-by-userid [this user] "Find a token relating to the user id")

  (invalidate-token [this user] "Make toke invalid")

  (save-token [this user] "Store refresh token for specified user id")
  
  (stop [this] "Close this storage"))

;;------------------------------------------------------------------
;; Datomic implementation of storage protocol
;;------------------------------------------------------------------
(defrecord DatomicStore [conn]
  Storage
  (save-user [this user]
    (timbre/info "::==> db.DatomicStore/save-user: " user "... ")
    
    (let [user-data (vector {:user/id (:user-id user)
                             :user/password (:password user)})
          res (d/transact conn user-data)]
      res))

  (load-user [this user]
    (timbre/info "::==> db.DatomicStore/load-user: " user "... ")
    (d/q  user-query (d/db conn) (:user-id user)))


  (find-token-by-userid [this user]
    (d/q token-query (d/db conn) (:user-id)))

  (invalidate-token [this user]
    )


  (save-token [this user]
    (d/transact conn (vector {:token/val (:token user)
                              :token/iat (:iat user)
                              :token/uref (:user-id user)})))

  (stop [this]
    (d/shutdown true)))



;;------------------------------------------------------------------
;; Return a concrete implementation of the Storage protocol
;;------------------------------------------------------------------
(defn start-db
  "Connesct Returns a datomic db instance"
  []
  (let [db-uri "datomic:mem://credentials"
        uri? (d/create-database db-uri)
        conn (d/connect db-uri)]
    (d/transact conn schema/user-schema)
    (->DatomicStore conn)))


;;------------------------------------------------------------------
;; Stops database providing peristance for storage protocol
;;------------------------------------------------------------------
(defn stop-db
  [db]
  (timbre/info "::==> db.DatomicStore/stop-db: sopping database" ))


;;------------------------------------------------------------------
;; Lifecyle managemet of storage as state
; using mount
;;------------------------------------------------------------------
(defstate storage
  :start (start-db)
  :stop (.stop storage))
