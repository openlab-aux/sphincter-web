(ns sphincter-web.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]])
  (:import [goog.storage.mechanism HTML5LocalStorage]))


(defn listen [el type]
  (let [out (chan)]
    (events/listen el type
      (fn [e] (put! out e)))
    out))

(defn get-token []
  "Retuns the token if it exists in LocalStorage, nil otherwise."
  (let [t (.get (HTML5LocalStorage.) "token")]
    (if t
      t
      ))

(.log js/console (.get (get-token) "foo"))
