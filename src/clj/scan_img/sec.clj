(ns scan-img.sec
  (:require [clojure.java.io :as io]
            [scan-img.user :as user]
            [scan-img.tokens :as tokens]
            [buddy.sign.jws :as bdjws]
            [buddy.core.keys :as bdkys]
            [ring.util.response :as resp]))

;;---------------------------------------------------------------------
;; Config
;;
;; openssl genpkey -algorithm RSA -out auth_priv_key.pem -aes-128-cbc -pass pass:xyz123
;; openssl rsa -pubout -in auth_priv_key.pem -out auth_pub_key.pem
;;---------------------------------------------------------------------
(def auth-conf {:privkey-pem "/resources/sec/auth_priv_key.pem"
                :pubkey-pem  "/resouces/sec/auth_pub_key.pem"
                :passphrase "dpspasswd"})

;;----------------------------------------------------------------------
;; Get auth token from session and associate as user to request
;;----------------------------------------------------------------------
(defn refresh-auth-token
  "Invalidate refresh toke and generate a new pair"
  [refresh-token]
  (if-let [unsigned (tokens/unsign-token refresh-token auth-conf)]
    (let [db-token (tokens/token-by-userid unsigned)
          db-user (user/load-user db-token)]
      (if (:valid db-token)
        (do
          (tokens/invalidate-token! (:id db-token))
          [true (tokens/gen-token-pair! auth-conf db-user)])
        [false {:message "Refresh token revoked or already exists"}]))
    [false {:message "Invalid or expired refresh token"}]))

;;----------------------------------------------------------------------
;; Get auth token from session and associate as user to request
;;----------------------------------------------------------------------
(defn- handle-token-refresh [handler req refresh-token]
  (let [[ok? res] (refresh-auth-token refresh-token)
        user (:user (when ok? (unsign-token (-> res :token-pair :auth-token))))]
    (if user
      (-> (handler (assoc req :auth-user user))
          (assoc :session {:token-pair (:token-pair res)}))
      {:status 302
       :headers {"Location " (str "/login?m=" (:uri req))}})))

;;----------------------------------------------------------------------
;; Get auth token from session and associate as user to request
;;----------------------------------------------------------------------
(defn wrap-authentication-token [handler]
  (fn [req]
    (let [auth-token (-> req :session :token-pair :authentication-token)
          unsigned-auth (when auth-token (unsign-token auth-token))]
      (if unsigned-auth
        (handler (assoc req :authenticated-user (:user unsigned-auth)))
        (handler req)))))

;;----------------------------------------------------------------------
;; Get auth token from session and associate as user to request
;;----------------------------------------------------------------------
(defn wrap-authentication [handler]
  (fn [req]
    (if (:auth-user req)
      (handler req)
      (let [refresh-token (-> req :session :token-pair :refresh-token)]
        (if refresh-token
          (handle-token-refresh handler req refresh-token)
          {:status 302
           :headers {"Location " (str "/login?m=" (:uri req))}})))))

;;----------------------------------------------------------------------
;; The authfn is responsible for the second step of authentication.
;; It receives the parsed auth data from request and should return a logical true
;;----------------------------------------------------------------------
(defn authenticate-user
  "uses the submitted user name to retrieve user credentials and
   compares with the submitted password provided by the user"
  [req]
  (println (str "sec::--> authenticate-user" req))
  (when-let [submitted-name (get-in req [:form-params :user-name])]
    (let [stored-user (user/load-user {:user-name submitted-name})
          submitted-password (get-in req [:form-params :password])]
      (when (and (some? submitted-password) (= submitted-password (:password stored-user)))
        submitted-name))))

;;----------------------------------------------------------------------
;; The authfn is responsible for the second step of authentication.
;; It receives the parsed auth data from request and should return a logical true
;;----------------------------------------------------------------------
(defn unauth-fn
  "Catches the unauthorised exception. There are two possibilities
   1: the user is authenticated though the resource is forbidden (403)
   2: The user is not authenticated so must log in"
  [request meta-data]
  (println (str "sec::--> authenticate-user" request))
  (cond
    (auth/authenticated? request) (resp/redirect "/forbidden")
    :else (resp/redirect "/show-login")))

;;----------------------------------------------------------------------
;; Authentication back-end
;;----------------------------------------------------------------------
(def auth-backend
  (sessbnd/session-backend {:unauthorized-handler unauth-fn}))
