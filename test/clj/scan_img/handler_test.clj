(ns scan-img.handler_test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as client]
            [scan-img.handler :as handler]))



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
                  (mock/body "{:code \"code\"
                               :name \"s34234\"
                               :password \"wrteert\"}")))


;; 
(deftest code-upload
  (testing "edn"
    (is (not= nil (handler/handler code-req) ))))



;;
(deftest read-config
  (testing "That config file is loaded"
    (is (not= nil (handler/read-config (mock/request :get "/get/config"))))))
