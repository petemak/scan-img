(ns scan-img.sec
  (:require [buddy.auth.backends.httpbasic :as httpbasic]
            [scan-img.db :refer [storage]]))



(defn authenticate-session
  [req]
  (when-let [user-name (get-in req [:body :user-name])]
    (let [user (.load-user storage {:user-name user-name})
          password (get-in req [:body :password])]
      (when (= (:password user) password)
        user-name))))

(def auth-backend
  (httpbasic/http-basic-backend {:realm "scan-image-main"
                                 :authfn authenticate-session}))
