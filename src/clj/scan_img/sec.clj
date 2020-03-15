(ns scan-img.sec
  (:require [clojure.java.io :as io]
            [scan-img.user :as user]
            [scan-img.tokens :as tokens]
            [buddy.sign.jws :as bdjws]
            [buddy.core.keys :as bdkys]
            [taoensso.timbre :as timbre]
            [ring.util.response :as resp]
            [ring.middleware.session :refer [wrap-session]]            
            [ring.middleware.session.cookie :refer [cookie-store]]))


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
;; Use a session storage engine that stores session data an
;; encrypted cookies.
;;----------------------------------------------------------------------
(defn wrap-auth-cookie
  [handler enc-key]
  "Wraps hndler with an encrypted cookie for storing session data. 
     enc-key - The secret key to encrypt the session cookie. Must be exactly 16 bytes."
  (-> handler
      (wrap-session
       {:store (cookie-store {:key enc-key})
        :cookie-name "scan-image"
        :cookie-attrs {:max-age (* 14 24 60 60)}})))



;;----------------------------------------------------------------------
;; If authentication token in request then unsign and 
;;----------------------------------------------------------------------
(defn wrap-token
  "Retunrs a handler function that gets authentication token from the
  session unsigns it and associates it as [:authenticated-user:user]
  to the request. Then calls the handler with the request

  Otherwise if no authentication token then simply calls handler with the "
  [handler] 
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
      (-> (handler (assoc req :authenticated-user refreshed-auth-token))
          (assoc :session {:token-pair (:token-pair refreshed-token)}))
      {:status 302
       :headers {"Location " (str "/login?m=" (:uri req))}})))



;;----------------------------------------------------------------------
;; Gets the refresh token from session and associate as user to request
;;----------------------------------------------------------------------
(defn wrap-authenticate-user
  "Returns a handler that checks for the authenticated user
  :auth-user in the request. Note: :auth-user is placed during
  login.
  If found calls next handler
  If not then checks for a refresh token in the session
  [:session :token-pair :refresh-token] and refreshes it
  by calling handle-token refresh.
  Last resort is to :staus 302 and /login"
  [handler]
  (fn [req]
    (if (:authenticated-user req)
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
  (timbre/info "::--> sec/login-user - credentials: " credentials)
  (let [[authenticated? authed-user] (user/authenticate-user credentials)]
    (if authenticated?
      [true (tokens/gen-token-pair! auth-conf authed-user)]
      [false authed-user])))


