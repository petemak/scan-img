(ns middleware-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as client]
            [scan-img.dummy-handlers :as dh]
            [scan-img.sec :as sec]
            [ring.util.response :as ring-resp]))



(def app (-> dh/body-echo-handler
             (sec/wrap-authenticated-req)))



(deftest auth-test
  (testing "when a request is submitted"
    (testing "and its anauthenticated"
      (let [response (app (mock/request :get "/"))]
        (is (= 400 (:status response)))))))
