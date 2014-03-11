;; Doesn’t werk. :(

(ns sphincter-web.test.core
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [sphincter-web.core :as core]))

(deftest foo-test
  (is (nil? (fn [] nil))))

(t/test-ns 'sphincter-web.test.core)
