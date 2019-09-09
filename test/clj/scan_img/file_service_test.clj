(ns scan-img.file-service-test)

(def s (slurp "README.md"))
(def data {:file-data s
           :file-name "docker-file"
           :file-type :docker-text
           :user-name "test-usr"
           :user-password "test-pwd"})
