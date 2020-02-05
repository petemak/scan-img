(ns scan-img.sec
  (:require [buddy.auth :as auth]
            [ring.util.response :as resp]
            [buddy.auth.backends.httpbasic :as httpbasic]
            [scan-img.db :refer [storage]]))



;;----------------------------------------------------------------------
;; Authentication handler. checks if the session is authenticated or not.
;;----------------------------------------------------------------------
(defn wrap-authenticated-req
  [handler]
  (fn [req]
    (if (auth/authenticated? req)
      (handler req)
      (resp/redirect "/login"))))

;;----------------------------------------------------------------------
;; The authfn is responsible for the second step of authentication.
;; It receives the parsed auth data from request and should return a logical true
;;----------------------------------------------------------------------
(defn authenticate-session
  "uses the user name to retrieve user credentials and
   compares with the password provided by the user"
  [req]
  (when-let [user-name (get-in  [:body :user-name])]
    (let [user (.load-user storage {:user-name user-name})
          password (get-in req [:body :password])]
      (when (and (some? password) (= (:password user) password))
        user-name))))

(def auth-backend
  (httpbasic/http-basic-backend {:realm "scan-image-main"
                                 :authfn authenticate-session}))
