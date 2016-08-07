(ns kbo.pages.frontpage)

(defn render []
  [:div [:h2 "Welcome to kbo1"]
   [:div [:a {:href "/about"} "go to about page"]]])
