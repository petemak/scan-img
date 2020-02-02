(ns scan-img.db
  (:require [buddy.hashers :as bh]
            [mount.core :refer [defstate]]))

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
  (close [this] "Close this storage"))

;;------------------------------------------------------------------
;; Datomic implementation of storage protocol
;;------------------------------------------------------------------
(defrecord DatomicStore [conn]
  Storage
  (save-user [this user]
    (println "::--> DatomicStore/save-user: " this)
    (if (some? (:user-name user))
      (:user-name user)))

  (load-user [this user]
    (println "::--> DatomicStore/laod-user: " this)
    (if (some? (:user-name user))
      (assoc user :secret "secret")))

  (close [this]
    ;; Implementation (d/shutdown)
    ))



;;------------------------------------------------------------------
;; Return a concrete implementation of the Storage protocol
;;------------------------------------------------------------------
(defn start-db
  "Returns a datomic db instance"
  []
  (DatomicStore. {}))



;;------------------------------------------------------------------
;; Stops database providing peristance for storage protocol
;;------------------------------------------------------------------
(defn stop-db
  []
  )


;;------------------------------------------------------------------
;; Lifecyle managemet of storage as state
; using mount
;;------------------------------------------------------------------
(defstate storage
  :start (start-db)
  :stop (stop-db))
