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
(def auth-conf {:privkey-pem "auth_priv_key.pem"
                :pubkey-pem  "auth_pub_key.pem"
                :passphrase "dpspasswd"})



;;----------------------------------------------------------------------
;; Usign token from session and associate as user to request
;;----------------------------------------------------------------------
(defn unsign-token [token]
  (bdjws/unsign token (bdkys/public-key (io/resource (:pubkey-pem auth-conf)))))


;;----------------------------------------------------------------------
;; Get auth token from session and associate as user to request
;;----------------------------------------------------------------------
(defn refresh-auth-token
  "Invalidate refresh toke and generate a new pair"
  [token]
  (if-let [unsigned-token (tokens/unsign-token token auth-conf)]
    (let [db-token (tokens/token-by-userid unsigned-token)
          db-user (user/load-user db-token)]
      (if (:valid db-token)
        (do
          (tokens/invalidate-refresh-token! (:id db-token))
          [true (tokens/gen-token-pair! auth-conf db-user)])
        [false {:message "Refresh token revoked or already exists"}]))
    [false {:message "Invalid or expired refresh token"}]))

;;----------------------------------------------------------------------
;; Get auth token from session and associate as user to request
;;----------------------------------------------------------------------
(defn wrap-token [handler]
  (fn [req]
    (let [auth-token (-> req :session :token-pair :authentication-token)
          unsigned-auth (when auth-token (unsign-token auth-token))]
      (if unsigned-auth
        (handler (assoc req :authenticated-user (:user unsigned-auth)))
        (handler req)))))



;;----------------------------------------------------------------------
;; Refreshes the refresh token, then unsigh
;;----------------------------------------------------------------------
(defn- handle-token-refresh [handler req token]
  (let [[ok? refreshed-token] (refresh-auth-token token)
        refreshed-auth-token (:user (when ok? (unsign-token (-> refreshed-token
                                                                :token-pair
                                                                :authentication-token))))]
    (if refreshed-auth-token
      (-> (handler (assoc req :auth-user refreshed-auth-token))
          (assoc :session {:token-pair (:token-pair refreshed-token)}))
      {:status 302
       :headers {"Location " (str "/login?m=" (:uri req))}})))



;;----------------------------------------------------------------------
;; Gets the refresh token from session and associate as user to request
;;----------------------------------------------------------------------
(defn wrap-authenticate-user [handler]
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
(defn login-user
  "uses the submitted user name to retrieve user credentials and
   compares with the submitted password provided by the user"
  [credentials]
  (println (str "::--> sec/authenticate-user" credentials))
  (let [[authenticated? authed-user] (user/authenticate-user credentials)]
    (if authenticated?
      [true (tokens/gen-token-pair! auth-conf authed-user)]
      [false authed-user])))


