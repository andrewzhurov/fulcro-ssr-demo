(ns org.cu.demo.task.ui.task-page
  (:require
   [com.fulcrologic.fulcro.components :as c :refer [defsc]]
   [com.fulcrologic.fulcro.react.hooks :as hooks]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [dv.fulcro-reitit :as fr]
   [org.cu.demo.task.ui.task-item :refer [TaskForm2 ui-task-form2]]
   [org.cu.demo.task.ui.task-grid :refer [ui-task-list TaskList TaskItem TaskGrid ui-task-grid]]
   [org.cu.demo.baseui :refer [ui-list-heading ui-button button-size]]
   [taoensso.timbre :as log :refer [spy]]))

(defsc TaskPage [this {:keys [task-grid #_task-list task-form] :as props}]
  {:query             [{:task-grid (c/get-query TaskGrid)}
                       #_{:task-list (c/get-query TaskList)}
                       {:task-form (c/get-query TaskForm2)}]
   :route-segment     ["tasks"]
   ::fr/route         [^:alias ["/" {:name :default :segment ["tasks"]}]
                       ["/tasks" {:name :tasks :segment ["tasks"]}]]
   :initial-state     (fn [_] {:task-grid (c/get-initial-state TaskGrid)
                               ;; :task-list (c/get-initial-state TaskList)
                               :task-form (c/get-initial-state TaskForm2)})
   :componentDidMount (fn [this] (df/load! this :all-tasks TaskItem {:refresh [this TaskPage TaskGrid :all-tasks]}))
   :ident             (fn [_] [:component/id :task-page])}
  (js/console.log "task page props" props)
  (js/console.log "task grid" task-grid)
  (js/console.log "task form" task-form)
  [:div
   [:div {:style {:display         "flex"
                  :flex-direction  "row"
                  :justify-content "space-between"
                  :align-items     "center"}}
    [:h2 {:style {:margin-bottom "0px"}} "Tasks"]
    (ui-task-form2 (spy task-form))]
   #_(ui-list-heading
      #js {"heading"     "Tasks"
           "endEnhancer" (fn []
                           #_(ui-button #js {:size  (.-compact button-size)
                                             :shape (.-pill button-size)}
                                        "Create"))})
   (ui-task-grid task-grid)
   #_(ui-task-list task-list)])
