(ns org.cu.demo.main-ws
  (:require
    [com.fulcrologic.fulcro.components :as c :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as fm]
    [nubank.workspaces.card-types.fulcro3 :as f3]
    [nubank.workspaces.core :as ws]
    [reagent.core :as re]))

(defsc FulcroDemo
  [this {:keys [counter]}]
  {:initial-state (fn [_] {:counter 0})
   :ident         (fn [] [::id :demo])
   :query         [:counter]}
  [:div
   (str "Fulcro counter demo [" counter "]")
   [:button {:on-click #(fm/set-value! this :counter (inc counter))} "+"]])

(ws/defcard fulcro-demo-card
  (f3/fulcro-card
    {::f3/root       FulcroDemo
     ::f3/app        {:render-middleware (fn [_ render] (re/as-element (render)))}
     ::f3/wrap-root? true}))

(defn main [] (ws/mount))
