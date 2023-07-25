(ns org.cu.demo.task.ui.task-grid
  (:require
   [clojure.spec.alpha :as s]
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.components :as c :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div]]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.ui-state-machines :as sm]
   [dv.cljs-emotion-reagent :refer [defstyled]]
   [dv.fulcro-util :as fu]
   [dv.fulcro-entity-state-machine :as fmachine]
   [org.cu.demo.task.data-model :as dm]
   [org.cu.demo.baseui :refer [ui-form-control ui-input
                               ui-button
                               ui-card ui-card-styled-body ui-card-styled-action
                               ui-block
                               ui-heading-level ui-heading
                               ui-dnd-list ui-dnd-list-stateful
                               ]]
   [taoensso.timbre :as log :refer [spy]]
   ))

(defstyled flex :div
  {:display     "flex"
   :align-items "center"
   "> * + *"    {:margin-left "0.5em"}})

(defstyled bold :div
  {:font-weight "700"})

(defsc TaskItem
  [this {:task/keys [id description] :ui/keys [show-debug?]}]
  {:query [:task/id :task/description :ui/show-debug?]
   :ident :task/id}
  (dom/div description)
  #_
  (ui-card
   #js {}
   (ui-card-styled-body
    #js {}
    (pr-str description)))
  #_[:div.ui.segment
     [:h4.ui.header "Task Item"]
     [flex [bold "ID: "] [:span (pr-str id)]]
     [flex {:style {:margin-bottom "1em"}} [bold "Description: "] [:span (pr-str description)]]
     [:button.ui.button.mini {:on-click #(m/toggle!! this :ui/show-debug?)}
      (str (if show-debug? "Hide" "Show") " debug")]
     (fu/props-data-debug this show-debug?)])

(def ui-task-item (c/factory TaskItem {:keyfn :task/id}))

(defsc TaskList [this {:keys [all-tasks]}]
  {:initial-state {}
   :query         [{:all-tasks (c/get-query TaskItem)}]}
  [:div "This is the list of tasks"
   [:div.ui.divider]
   (map ui-task-item all-tasks)])

(def ui-task-list (c/factory TaskList))

(defn task-item-card
  [{:task/keys [id description]}]
  [:div.ui.card {:key id}
   [:div.content>div.ui.tiny.horizontal.list>div.item description]])

(defsc TaskGrid [this {:keys [all-tasks]}]
  {:query         [{[:all-tasks '_] (c/get-query TaskItem)}]
   :initial-state {}}
  (js/console.log "all tasks" all-tasks)
  (when (not-empty all-tasks)
    (dom/div
     #js {:key (count all-tasks)}
     (ui-dnd-list-stateful
      (clj->js {"initialState"    {"items" (reverse (map ui-task-item all-tasks))}
                "removable"       true
                "removableByMove" true})))))

(def ui-task-grid (c/factory TaskGrid))
