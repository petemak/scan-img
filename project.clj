(defproject scan-img "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.8"]
                 [compojure "1.6.1"]
                 [yogthos/config "1.1.2"]
                 [ring "1.7.1"]
                 [cljs-ajax "0.8.0"]
                 [fogus/ring-edn "0.3.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-ring "0.12.5"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler scan-img.handler/dev-handler}

  ;; :ring {:handler scan-img.server/app-routes}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [peridot "0.5.1"]
                   [clj-http "3.10.0"]
                   [ring/ring-mock "0.4.0"]]
    :plugins      [[lein-figwheel "0.5.18"]]}
    :prod { }
    :uberjar {:source-paths ["env/prod/clj"]
              :omit-source  true
              :main         scan-img.server
              :aot          [scan-img.server]
              :uberjar-name "scan-img.jar"
              :prep-tasks   ["compile" ["cljsbuild" "once" "min"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "scan-img.core/mount-root"}
     :compiler     {:main                 scan-img.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            scan-img.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})
