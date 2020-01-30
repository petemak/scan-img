(ns scan-img.db
  (:require [buddy.hashers :as bh]))


(defprotocol Storage
  "Protoco for persisting data"
  (save-user [this user] "Load specified user credentials. 
                          Expected key :user-name and :secret")

  (load-user [this user] "Saves specified user credentials. 
                          Expected keys :user-name"))


(defrecord DatomicStore []
  Storage
  (save-user [_ user]
    (:user-name user))

  (load-user [_ user]
    (:secret user)))
