(ns scan-img.handler_test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as client]))



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
