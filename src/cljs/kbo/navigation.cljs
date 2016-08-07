(ns kbo.navigation
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType]
            [kbo.state :as state]
            [kbo.page-dispatcher :as d]
            [bidi.bidi :as bidi]
            [reagent.core :as r]
            [promesa.core :as p])
  (:import [goog Uri]
           [goog.history Html5History]))

(def routes ["/" {""                    :navigation/frontpage
                  ["ejendom/"]          :navigation/about
                                        ;    ["ejendom/" :id "/sager"] :navigation/ejendomssager
                  }])

(defn- navigation-changed! [dispatchers path]
  (let [{:keys [handler route-params] :as route} (bidi/match-route routes path)
        dispatcher-fn (or (get dispatchers handler)
                          #(p/resolved {}))
        ui-state (or (get @state/state path)
                     (r/atom {}))]
    (swap! state/state merge {:page-state nil
                              :navigation route
                              path        ui-state})
    (-> (:configuration-promise (:progress @state/state))
        (p/then #(dispatcher-fn (assoc route-params :configuration (:configuration @state/state))))
        (p/then #(swap! state/state assoc :page-state %)))))

(defn- get-path [window]
  (let [fragment (-> (Uri. (.. window -location -href))
                     (.getFragment))]
    (if (= "" fragment) "/" fragment)))

(defn path-for-route [route]
  (when-let [{:keys [route-params handler]} route]
    (apply bidi.bidi/path-for routes handler (->> (map vec route-params)
                                                  flatten))))

(defn render-route [handler & args]
  (->> (try (apply bidi/path-for routes handler args)
            (catch js/Error _ ""))
       (str "#")))

(defn init! [dispatchers]
  (let [history (Html5History. js/window)]
    (events/listen history EventType/NAVIGATE #(navigation-changed! dispatchers (get-path js/window)))
    (.setEnabled history true)))
