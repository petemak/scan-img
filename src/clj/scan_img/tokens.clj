(ns scan-img.tokens
  (:require [clj-time.core :as cljt]
            [clojure.java.io :as io]
            [buddy.sign.jws :as bdjws]
            [buddy.sign.util :as bdutl]
            [buddy.core.keys :as bdkys]
            [scan-img.db :refer [storage]]))



;;----------------------------------------------------------------------
;; Private key
;;----------------------------------------------------------------------
(defn- priv-key
  "Loads private key using specified configuration"
  [auth-conf]
  (bdkys/private-key
   (io/resource (:privkey-pem auth-conf))
   (:passphrase auth-conf)))


;;----------------------------------------------------------------------
;; Public key
;;----------------------------------------------------------------------
(defn- pub-key
  "Loads public key using specified configuration"  
  [auth-conf]
  (bdkys/public-key
   (io/resource (:pubkey-pem auth-conf))))


;;----------------------------------------------------------------------
;; Get auth
;;----------------------------------------------------------------------
(defn unsign-token
  "Usigns token using the pblic key"
  [token config]
  (->> (io/resource (:pubkey-pem config))
       (bdkys/public-key )
       (bdjws/unsign token )))



;;----------------------------------------------------------------------
;; Generate and auth token
;;----------------------------------------------------------------------
(defn gen-auth-token!
  "Generates an authentication token with an expiry time of 30 minutes"
  [auth-conf user]
  (let [exp (-> (cljt/plus (cljt/now) (cljt/minutes 30))
                (bdutl/to-timestamp))]
    (bdjws/sign {:user (dissoc user :password)}
                (priv-key auth-conf)
                {:alg :rs256 :exp exp})))

;;----------------------------------------------------------------------
;; Generate and auth token
;;----------------------------------------------------------------------
(defn gen-refresh-token!
  "Generates a refresh token with an expiry time of 7 days"  
  [auth-conf user]
  (let [iat (bdutl/to-timestamp (cljt/now))
        token (bdjws/sign {:user-id (:id user)}
                          (priv-key auth-conf)
                          {:alg :rs256
                           :iat iat
                           :exp (-> (cljt/plus (cljt/now) (cljt/days 7))
                                    (bdutl/to-timestamp))})]

    (.save-token storage (-> user
                             (assoc :issued iat)
                             (assoc :token token)))
    token))

;;----------------------------------------------------------------------
;; Generate an auth and a refresh pair
;;----------------------------------------------------------------------
(defn gen-token-pair!
  "Generate an auth and a refresh toke pair"
  [auth-conf user]
  {:token-pair {:auth-token (gen-auth-token! auth-conf user)
                :refresh-token (gen-refresh-token! auth-conf user)}})


;;----------------------------------------------------------------------
;; Find token of specified user
;;----------------------------------------------------------------------
(defn token-by-userid
  "Find token assigned to the specified user"
  [user]
  (let [datom (.find-token-by-userid storage user)]
    {:id (first (first datom))
     :token (second (first datom))}))


(defn invalidate-refresh-token!
  [user])
