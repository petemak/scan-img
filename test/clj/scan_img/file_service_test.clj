(ns scan-img.file-service-test
  (:require [clojure.test :refer :all]
            [scan-img.file-service :as fs]))

(def s (slurp "README.md"))
(def   (slurp test/cls/scan_image/sample-config.txt))

(def cfg {:name "Docker Image Scanner"
          :port 3000
          :mode :dev
          :executable-cmd [["docker" "build" "-f" "{{cannonical-path}}" "-t" "{{file-name}}:1.0"]
			   ["docker" "run" "-v" "/var/run/docker.sock:/var/run/docker.sock"
                            "docker-aqua-local.artifact.swissre.com/aqua-scanner-swissre:4.0"
                            "scan" "-H" "https://docker-scan-np.swissre.com"
                            "-U" "scanfromui"  "-P" "sr1234567"
                            "--collect-sensitive" "--local" "{{file-name}}:1.0"]]})

(def data {:file-data s
           :file-name "docker-file"
           :image-name "iggle-piggle"
           :cannonical-path "/wewe/werwer/xyz-img"
           :file-type :docker-text
           :user-name "test-usr"
           :user-password "test-pwd"})

(comment
  (deftest save-run-command
    (testing "Whole saving and running commands"
      (let [results (fs/sync-reg-code-event s "test" "testpwd")]
        (is (not= nil results))
        (is (not= nil (:cannonical-path results)))))))


(deftest read-config
  (testing "reading the configuration from file system"
    (let [cfg (fs/read-config)]
      (is (not= nil cfg))
      (is (not= nil (:resutls cg)))
      (is (= (:message (first (:results cfg))) "Config file loaded")))))
