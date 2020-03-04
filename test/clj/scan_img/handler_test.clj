(ns scan-img.handler_test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as client]
            [scan-img.handler :as handler]
            [scan-img.server :as svr]))



(defn tes-upload
  []
  (client/post "http://localhost:3449/upload" {:multipart
                                               [{:name "title" :content "Image file"}
                                                {:name "Content/type" :content "image/jpeg"}
                                                {:name "README.md" :part-name "image-file" :content "image"}
                                                {:name "file" :content (clojure.java.io/file "README.md")}]
                                                 ;; You can also optionally pass a :mime-subtype
                                                 ;;:mime-subtype "foo"
                                     
                                                                       }))


;; Mock POST "upload/code"
(def code-req (-> (mock/request :post "/upload/code")
                  (mock/content-type "application/edn")
                  (mock/body (prn-str {:user-id "test123"
                                       :password "werwer"}))))


(def login-req (-> (mock/request :post "/login")
                   (mock/content-type "application/edn")
                   (mock/body (prn-str {:user-id "test123"
                                        :password "testpwd"}))))




(defn server-fixture
  [f]
  (try
    (svr/start-sever)
    (f)
    (svr/stop-server)))


(use-fixtures :once server-fixture)

;; 
(deftest code-upload
  (testing "edn"
    (is (not= nil (handler/handler code-req) ))))



;;
(deftest read-config
  (testing "That config file is loaded"
    (is (not= nil (handler/read-config (mock/request :get "/get/config"))))))



(deftest login
  (testing "Testing login"
    (testing "That login for a registerred users returns true")))
