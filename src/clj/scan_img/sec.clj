(ns scan-img.sec
  (:require [buddy.auth :as auth]
            [ring.util.response :as resp]
            [buddy.auth.backends.httpbasic :as httpbasic]
            [scan-img.user :as user]
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
  "uses the submitted user name to retrieve user credentials and
   compares with the submitted password provided by the user"
  [req]
  (when-let [submitted-name (get-in req [:form-params :user-name])]
    (let [stored-user (user/load-user {:user-name submitted-name})
          submitted-password (get-in req [:form-params :password])]
      (when (and (some? submitted-password) (= submitted-password (:password stored-user)))
        submitted-name))))

(def auth-backend
  (httpbasic/http-basic-backend {:realm "scan-image-main"
                                 :authfn authenticate-session}))
