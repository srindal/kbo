(ns kbo.views.application
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [goog.events :as events]
    [kbo.state :as state]
    [kbo.components.keyboard :as k]))


;Used indirectly - toggles stateviewer. Don't remove ;)
(defonce state-view-listener-key (events/listen js/window "keydown"
                                                (fn [e]
                                                  (cond
                                                    (k/F10? e) (swap! state/state update-in [:system :show-design?] not)
                                                    (k/F12? e) (swap! state/state update-in [:system :show-state?] not)
                                                    (k/esc? e) (swap! state/state update-in [:system] assoc :show-state? false :show-design? false)))))


(defn view [{:keys [on-login pages]} {:keys [page-state navigation user system configuration]} ui-state-atom]
  (let [{:keys [handler route-params]} navigation]
    [:div {:style {:display         "flex"
                   :flex-direction  "column"
                   :justify-content "space-between"
                   :height          "100%"
                   :width           "100%"}}
     [:div {:style {:height "100%"}}

      (when-let [page (get pages handler)]
        [page page-state ui-state-atom route-params configuration])
      ]]))
