(ns scan-img.ui-stm-test
  (:require [clojure.test :refer :all]
            [scan-img.ui-stm :as stm]
            [fsmviz.core  :as fsm]))



(fsmviz.core/generate-image stm/stm  "fsm")
