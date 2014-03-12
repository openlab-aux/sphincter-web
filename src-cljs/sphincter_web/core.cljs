(ns sphincter-web.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [dommy.macros :refer [sel1 node deftemplate]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [dommy.core :as dommy]))

;;;; Helpers

(defn listen [el type]
  "Adds a listener to element el, returns a channel that fires on event type."
  (let [out (chan)]
    (events/listen el type
      (fn [e] (put! out e)))
    out))

(defn get-token []
  "Retuns the token if it exists in localStorage, nil otherwise."
  (.getItem js/localStorage "token"))

(defn set-token [token]
  "Sets the token in localStorage."
  (.setItem js/localStorage "token" token))

(defn query-sphincter [token type]
  "Queries sphincter to open/close the lab."
  :success) ;; TODO

;;;; UI Markup

;; Open/Close layout
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

;; New token layout
(deftemplate new-token [message]
   [:p message]
   [:input#token-input {:placeholder "Token eingeben"}]
   [:button#done "Fertig"])

;;;; UI Logic

(defn log [obj]
  (.log js/console obj))

(defn token-ui [parent message]
  "Asks for a token; saves it once given."
  (go-loop [mes message]
           (dommy/replace-contents! parent (new-token mes))
           (let [button (sel1 :#done)
                 _ (<! (listen button "click"))
                 token (.-value (sel1 :#token-input))]
             (if (= token "")
               (recur "Ein leerer Token macht keinen Sinn …")
               (do
                 (set-token token)
                 (open-close-ui parent))))))

(defn open-close-ui [parent]
  "Displays an open and a close button to open/close the hackerspace."
  (go-loop [result {}]
           (dommy/replace-contents! parent (buttons :result result))
           (let [open   (sel1 :#open)
                 close  (sel1 :#close)
                 openc  (listen open "click")
                 closec (listen close "click")
                 token  (get-token)]
             (when (not token)
               (token-ui parent "Bitte gib einen Token ein."))
             (let [action (alt!
                            openc  :open
                            closec :closed)
                   res (query-sphincter action)]
               (recur {:type res :action action})))))

;;;; Main

(open-close-ui (sel1 :main))
