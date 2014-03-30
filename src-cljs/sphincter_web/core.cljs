(ns sphincter-web.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [dommy.macros :refer [sel1 node deftemplate]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [dommy.core :as dommy]
            [ajax.core :refer [GET]]))

;;;; TEMP

(def sphincter-url "http://localhost:8000")

;;;; Helpers

(defn- listen [el type]
  "Adds a listener to element el, returns a channel that fires on event type."
  (let [out (chan)]
    (events/listen el type
      (fn [e] (put! out e)))
    out))

(defn- get-token []
  "Retuns the token if it exists in localStorage, nil otherwise."
  (.getItem js/localStorage "token"))

(defn- set-token [token]
  "Sets the token in localStorage."
  (.setItem js/localStorage "token" token))

(defn query-sphincter [url token type]
  "Queries sphincter to open/close the lab. `type` is :open or :close.
  Returns a channel that delivers a map with :type one of:failure,
  :success :not-allowed or :error after querying sphincter.
  In case of :error the map contains a :msg."
  (let [c (chan)]
    (GET url
         {:response-format :raw
          :format :raw
          :params {:action (name type) :token token}
          :handler (fn [res] (put! c {:type (case (str res)
                                      "SUCCESS" :success
                                      "FAILED" :failure
                                      "NOT ALLOWED" :not-allowed)}))
          :error-handler (fn [e] (put! c {:type :error
                                          :msg (str (:status e) "; " (:status-text e))}))})
    c))

;;;; UI Markup

;; Open/Close layout
(deftemplate buttons [& {:keys [result]}]
  [:div.success (str "Token: " (get-token))]
  (case (:type result)
    :success (let [t (case (:action result)
                       :open "offen"
                       :close "geschlossen")]
               [:div.success (str "Lab ist " t ".")])
    :failure (let [t (case (:action result)
                       :open "öffnen"
                       :close "schließen")]
               [:div.failure (str "Konnte das Lab nicht " t ".")])
    :error [:div.failure (str "Ein Fehler ist aufgetreten. Nachricht:\n" (:msg result))]
    nil)
  [:button#open "Open"]
  [:button#close "Close"])

;; New token layout
(deftemplate new-token [message]
   [:p message]
   [:input#token-input {:placeholder "Token eingeben"}]
   [:button#done "Fertig"])

;;;; UI Logic

(defn waiting-ui [parent msg chan]
  "Displays a message that an action is executing. `chan` is a channel that
  waits for the result of the action. The message will be shown in parent.
  Returns a channel with the result."
  (dommy/replace-contents! parent (node [:p msg]))
  (go
   (let [res (<! chan)]
     (dommy/clear! parent)
     res)))

(declare token-ui)
(declare open-close-ui)

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
           ; result: {:type :action :msg}
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
                            closec :close)
                   res (<! (waiting-ui
                              parent "Pinge Sphincter an …"
                              (query-sphincter sphincter-url token action)))]
               (when (= (:type res) :not-allowed)
                 (log "Not allowed")
                 (token-ui parent "Der Token ist falsch."))
               (recur (merge res {:action action}))))))

;;;; Main

(open-close-ui (sel1 :main))
