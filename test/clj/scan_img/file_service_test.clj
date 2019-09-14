(ns scan-img.file-service-test)

(def s (slurp "README.md"))

(def data {:file-data s
           :file-name "docker-file"
           :image-name "iggle-piggle"
           :cannonical-path "/wewe/werwer/xyz-img"
           :file-type :docker-text
           :user-name "test-usr"
           :user-password "test-pwd"})
