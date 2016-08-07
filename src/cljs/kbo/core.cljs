(ns kbo.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [kbo.page-dispatcher :as pd]
              [kbo.pages.frontpage :as frontpage]
              [kbo.pages.about :as aboutpage]
              [kbo.state :as state]
              [kbo.utils.util :as util]
              [kbo.navigation :as navigation]
              [kbo.views.application :as application]
              [bidi.bidi :as bidi]
              [reagent.core :as r]))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! state/state update-in [:__figwheel_counter] inc))

(def nav
  {:navigation/frontpage               [frontpage/render pd/frontpage]
   :navigation/ejendom                 [aboutpage/render pd/about]})


;; -------------------------
;; Initialize app

(defn app-view []
  (let [path (navigation/path-for-route (:navigation @state/state))
        ui-state-atom (or (get @state/state path)
                          (r/atom {}))]
    [application/view {:on-login :undefined ;(partial dispatcher/login (util/map-vals second nav))
                       :pages    (util/map-vals first nav)}
     @state/state ui-state-atom]))

(defn mount-root []
  (r/render-component [app-view] (js/document.getElementById "app"))
  (navigation/init! (util/map-vals second nav)))

(defn init! []

  (enable-console-print!)

;  (state/assoc-in! [:progress :configuration-promise] (dispatcher/boot))

 ; (state/assoc-in! [:progress :identity-promise] (dispatcher/login-from-local-token))

  (mount-root)
                                        ;(dispatcher/refresh-services-status)
  )
