(ns scan-img.file-service-test
  (:require [clojure.test :refer :all]
            [scan-img.file-service :as fs]))

(def s (slurp "README.md"))

(def data {:file-data s
           :file-name "docker-file"
           :image-name "iggle-piggle"
           :cannonical-path "/wewe/werwer/xyz-img"
           :file-type :docker-text
           :user-name "test-usr"
           :user-password "test-pwd"})


(deftest save-run-command
  (testing "Whole saving and running commands"
    (let [results (fs/sync-reg-code-event s "test" "testpwd")]
      (is (not= nil results))
      (is (not= nil (:cannonical-path results))))))
