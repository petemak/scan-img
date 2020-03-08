(ns foundation.ring-basics
  (:require [clj-http.client :as http]
            [ring.mock.request :as mock]
            [ring.util.response :as ring-response]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]))


(def test-user {:user-id "tst1"
                :password "passwd1"})


(def test-req (-> (mock/request :post "http://localhost:3000/login?q=clojure")
                  (mock/content-type "application/edn")
                  (mock/body (pr-str test-user))))



;; 
;;Middleware
;; will add
;;          :query-params {}
;;          :form-params {"form-params" "user-id=tst1", "password" "passwd1"}         
;;          :params      {"form-params" "user-id=tst1", "password" "passwd1"}
;;           
((wrap-params identity) test-req)














