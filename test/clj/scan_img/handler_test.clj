(ns scan-img.handler_test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as http]
            [scan-img.user :as usr]
            [scan-img.handler :as handler]
            [scan-img.server :as svr]))



(defn tes-upload
  []
  (http/post "http://localhost:3449/upload" {:multipart
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


(def test-user {:user-id "tst1"
                :password "passwd1"})


(def test-req (mock/request :post "http://localhost:3000/login" {:form-params test-user}))

(defn user-fixture
  "Set up and remove test user"
  [f]
  (usr/register-user test-user)
  (f)
  (usr/unregister-user test-user))

;; User fixture is run for each test
(use-fixtures :each user-fixture)

(defn server-fixture
  "start stop server"
  [f]
  (try
    (svr/start-sever)
    (f)
    (svr/stop-server)
    (catch Exception e
      (println (str "Error occurred: " (.getMessage e))))))


;; server fixture is run once
(use-fixtures :once server-fixture)


;;------------------------------------------------
;; Ring request
;; 
;; {:request-method :get
;;  :uri "/search"
;;  :query-string "q=clojure"
;;  :query-params {"q" "clojure"}
;;  :form-params {}
;;  :params {"q" "clojure"}}
;;------------------------------------------------

(deftest login
  "Test /login route"
  (testing "Testing login"
    (testing "That login for a registerred users returns true"
      (let [resp (http/post "http://localhost:3000/login" {:form-params test-user})]
        (is (= 302 (:status resp)))
         
        ))))

