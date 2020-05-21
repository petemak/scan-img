(ns scan-img.file-service-test
  (:require [clojure.test :refer :all]
            [scan-img.file-service :as fs]))

(def s (slurp "README.md"))
;;(def c (slurp "/test/clj/scan_image/sample-config.edn"))

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
  (testing "reading the configuration"
    (testing "that the image file configuration is correctly read"
      (let [cfg       (fs/resolve-command-config! {:file-type :docker-image})
            exec-cmds (:executable-cmds cfg)]
        (is (not= nil cfg))
        (is (not= nil exec-cmds))
        (is (= 1 (count exec-cmds)))
        (is (= "docker" (nth (first exec-cmds) 0)))
        (is (= "run"    (nth (first exec-cmds) 1)))
        (is (= "--user {{user-name}}" (nth (first exec-cmds) 2)))))
    (testing "that the docker file configuration is correctly read"
      (let [cfg       (fs/resolve-command-config! {:file-type :docker-text})
            exec-cmds (:executable-cmds cfg)]
        (is (not= nil cfg))
        (is (not= nil exec-cmds))
        (is (= 2 (count exec-cmds)))
        (is (= "docker" (nth (first exec-cmds) 0)))
        (is (= "build"  (nth (first exec-cmds) 1)))
        (is (= "-f"     (nth (first exec-cmds) 2)))))
    (testing "that the image file configuration is correctly read"
      (let [cfg       (fs/resolve-command-config! {:file-type :docker-text})
            exec-cmds (:executable-cmds cfg)]
        (is (not= nil cfg))
        (is (not= nil exec-cmds))
        (is (= 2 (count exec-cmds)))
        (is (= "docker" (nth (second exec-cmds) 0)))
        (is (= "run"    (nth (second exec-cmds) 1)))
        (is (= "--user {{user-name}}" (nth (second exec-cmds) 2)))))))


