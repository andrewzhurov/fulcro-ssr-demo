(ns org.cu.demo.task.ui.task-item
  (:require
   [clojure.spec.alpha :as s]
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.components :as c :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div]]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.ui-state-machines :as sm]
   [com.fulcrologic.fulcro.react.hooks :as hooks]
   [dv.cljs-emotion-reagent :refer [defstyled]]
   [dv.fulcro-util :as fu]
   [dv.fulcro-entity-state-machine :as fmachine]
   [org.cu.demo.auth.login :refer [session-join Session get-session]]
   [org.cu.demo.task.data-model :as dm]
   [org.cu.demo.baseui :refer [ui-form-control
                               ui-input
                               ui-text-area
                               ui-button button-size
                               ui-card ui-card-styled-body ui-card-styled-action
                               ui-block
                               ui-heading-level ui-heading
                               ui-modal ui-modal-header ui-modal-body ui-modal-footer ui-modal-button
                               ui-modal-focus-once modal-size modal-role]]
   [taoensso.timbre :as log :refer [spy]]
   ))

#_
(defsc TaskForm
  [this {:keys [server/message ui/machine-state ui/loading? ui/show-form-debug?] :as props}
   {:keys [cb-on-submit on-cancel]}]
  {:query             [:task/id :task/description fs/form-config-join
                       :ui/machine-state :ui/loading? :server/message
                       (sm/asm-ident ::form-machine)
                       :ui/show-form-debug?]
   :ident             :task/id
   :form-fields       #{:task/description}
   :initial-state     (fn [_] (fs/add-form-config
                               TaskForm (merge (empty-form)
                                               {:ui/show-form-debug? false})))
   :componentDidMount (fn [this] (fmachine/begin! this ::form-machine TaskItemReturn))}
  (let [{:keys [checked? disabled?]} (fu/validator-state this validator)]
    [:div
     #_(fu/notification {:ui/submit-state machine-state :ui/server-message message})
     #_(when goog.DEBUG
         (fu/ui-button #(m/toggle! this :ui/show-form-debug?) "Debug form"))
     #_(fu/form-debug validator this show-form-debug?)

     [:h3 nil "Enter a new task"]

     [:div.ui.form
      {:class    (when checked? "error")
       :onChange (fn [e] (sm/trigger! this ::form-machine :event/reset)
                   true)}
      [:div.field
       (fu/ui-textfield this "Task Description" :task/description props :tabIndex 0
                        :autofocus? true)]

      [:div.ui.grid
       [:div.column.four.wide>button
        {:tabIndex 0
         :disabled disabled?
         :onClick
         (fu/prevent-default
          #(let [task (dm/fresh-task props)]
             (fmachine/submit-entity! this
                                      {:entity          task
                                       :machine         ::form-machine
                                       :creating?       true
                                       :remote-mutation 'org.cu.demo.task/create-task
                                       :on-reset        cb-on-submit
                                       :target          {:append [:all-tasks]}})))


         :class (str "ui primary button" (when loading? " loading"))}
        "Enter"]

       (when on-cancel
         [:div.column.four.wide>button.ui.secondary.button.column
          {:on-click on-cancel} "Cancel"])]]]))
#_
(def ui-task-form (c/factory TaskForm {:keyfn :task/id}))

(defsc TaskItemReturn [this props]
  {:query [:server/message :server/error?
           :task/id
           :task/duration
           :task/scheduled-at
           :task/description]
   :ident (fn [_] [:component/id :task-item-return])})

(defn empty-form [] (dm/make-task {:task/description ""}))

(defn task-valid [form field]
  (let [v (get form field)]
    (s/valid? field v)))

(def validator (fs/make-validator task-valid))

(defsc TaskForm2 [this {:task/keys [id] :keys [session] :as props}
                  {:keys [cb-on-submit on-cancel]}]
  {:query             [:task/id :task/description
                       fs/form-config-join
                       :task-form/modal-open?
                       (sm/asm-ident ::form-machine)
                       {:session (c/get-query Session)}]
   :ident             :task/id
   :form-fields       #{:task/description}
   :initial-state     (fn [_] (merge (fs/add-form-config
                                      TaskForm2 (empty-form))
                                     {:task-form/modal-open? false}))
   :componentDidMount (fn [this] (js/console.log "MOUNTED") (fmachine/begin! this ::form-machine TaskItemReturn))}
  (js/console.log "props" props)

  (let [modal-open? (c/get-state this :modal-open?)
        description (or (c/get-state this :task/description) "")
        ]
    (js/console.log "description local" description)
    [:<>
     (when (:session/valid? session)) ;; TODO
     (ui-button
      #js {:size    (.-compact button-size)
           :shape   (.-pill button-size)
           :onClick #(c/update-state! this assoc :modal-open? true)}
      "New Task")
     (ui-modal
      #js {:onClose #(c/update-state! this assoc :modal-open? false)
           :isOpen  modal-open?}
      (ui-modal-header
       #js {}
       "New Task")
      #_(ui-modal-focus-once
         #js {})
      (ui-modal-body
       #js {}
       (ui-text-area
        #js {:placeholder "Description"
             :value       description
             :onChange    #(do (js/console.log %)
                               (c/update-state! this assoc :task/description (-> % .-target .-value)))}))
      (ui-modal-footer
       #js {}
       (ui-modal-button
        #js {:onClick
             (fu/prevent-default
              #(let [task (dm/fresh-task (assoc props
                                                :task/description description))]
                 (js/console.log "creating task:" task)
                 (c/update-state! this assoc :task/description "" :modal-open? false)
                 (fmachine/submit-entity! this
                                          {:entity          task
                                           :machine         ::form-machine
                                           :creating?       true
                                           :remote-mutation 'org.cu.demo.task/create-task
                                           :on-reset        cb-on-submit
                                           :target          {:append [:all-tasks]}})))}
        "Create")))]
    ))

(def ui-task-form2 (c/factory TaskForm2 {:keyfn :task/id}))
