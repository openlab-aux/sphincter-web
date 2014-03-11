(ns sphincter-web.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [sel1 node deftemplate]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [dommy.core :as dommy]))


(defn listen [el type]
  (let [out (chan)]
    (events/listen el type
      (fn [e] (put! out e)))
    out))

(defn get-token []
  "Retuns the token if it exists in localStorage, nil otherwise."
  (.getItem js/localStorage "token"))

;;;; Markup

(deftemplate buttons [& {:keys [result]}]
  (case (:type result)
    :success (let [t (case (:action result)
                       :open "offen"
                       :closed "geschlossen")]
               [:div.success (str "Lab ist " t ".")])
    :failure (let [t (case (:action result)
                       :open "öffnen"
                       :closed "schließen")]
               [:div.failure (str "Konnte das Lab nicht " t ".")])
    nil)
  [:button#open "Open"]
  [:button#close "Close"])

(deftemplate new-token [message]
   [:p message]
   [:input#token-input {:placeholder "Token eingeben"}]
   [:button#done "Fertig"])

;;;; Main

(defn -main []
  )
(dommy/replace-contents! (sel1 :main) (buttons))
(dommy/replace-contents! (sel1 :main) (buttons :result {:type :failure :action :open}))
