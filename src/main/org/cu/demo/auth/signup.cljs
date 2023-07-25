(ns org.cu.demo.auth.signup
  (:require
   [clojure.string :as str]
   [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop :refer [react-factory]]
   [com.fulcrologic.fulcro.components :as c :refer [defsc force-children]]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.ui-state-machines :as uism]
   [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
   [com.fulcrologic.fulcro.ui-state-machines :as sm :refer [defstatemachine]]
   [dv.fulcro-util :as fu]
   [dv.fulcro-reitit :as fr]
   [org.cu.demo.auth.session :as session]
   [org.cu.demo.auth.login :refer [session-join Session get-session]]
   [org.cu.demo.client.application :refer [SPA]]
   [org.cu.demo.baseui :refer [ui-base-provider
                               ui-theme-provider light-theme dark-theme
                               ui-navbar ui-navbar-item
                               ui-button
                               ui-avatar]]
   [taoensso.timbre :as log]

   ["@react-oauth/google" :refer [useGoogleOneTapLogin GoogleLogin googleLogout]]))

(def signup-ident [:component/id :signup])
(declare Signup)

(defn clear-signup-form*
  "Mutation helper: Updates state map with a cleared signup form that is configured for form state support."
  [state-map]
  (log/info "Clearing signup form")
  (-> state-map
    (assoc-in signup-ident
      {:account/email          ""
       :account/password       ""
       :account/password-again ""})
    (fs/add-form-config* Signup signup-ident)))

(defmutation clear-signup-form [_]
  (action [{:keys [state]}]
          (swap! state clear-signup-form*)))

(declare add-profile)

(defmutation signup [_]
  (action [{:keys [state]}]
          (log/info "Starting signup mutation")
          (swap! state
                 (fn [s]
                   (-> s
                       (fs/mark-complete* signup-ident)
                       (assoc-in [df/marker-table ::signup] {:status :loading})))))

  (ok-action [{:keys [app state result]}]
             (let [state   @state
                   session (fdn/db->tree (comp/get-query Session) [:component/id :session] state)]
               (log/info "Signup success result: " result)
               (df/remove-load-marker! app ::signup)
               (when (:session/valid? session)
                 (uism/trigger! app ::session/session :event/signup-success)
                 )))

  (Error-action [{:keys [app]}]
                (df/remove-load-marker! app ::signup))

  (remote [{:keys [state] :as env}]
          (let [{:account/keys [email password password-again]} (get-in @state signup-ident)]
            (let [valid? (boolean (and (fu/valid-email? email) (fu/valid-password? password)
                                       (= password password-again)))]
              (when valid?
                (-> env (m/returning Session)))))))

(defmutation auth-google [_]
  (action [{:keys [state]}]
          (log/info "Starting auth-google mutation"))

  (ok-action [{:keys [app state result]}]
             (js/console.log "result" result)
             (let [state   @state
                   session (fdn/db->tree (comp/get-query Session) [:component/id :session] state)]
               (log/info "Signup success result: " result)
               (when (:session/valid? session)
                 (uism/trigger! app ::session/session :event/signup-success)
                 (comp/transact! app [(add-profile (-> result :body :org.cu.demo.auth.signup/auth-google))]))))

  (error-action [{:keys [app]}]
                nil)

  (remote [{:keys [state] :as env}]
          (let [{:account/keys [email password password-again]} (get-in @state signup-ident)]
            (-> env (m/returning Session)))))

(defmutation
  mark-complete!* [{field :field}]
  (action [{:keys [state]}]
          (log/info "Marking complete field: " field)
          (swap! state fs/mark-complete* signup-ident field)))

(defn mark-complete!
  [this field]
  (comp/transact!! this [(mark-complete!* {:field field})]))

(defn signup-valid [form field]
  (let [v (get form field)]
    (case field
      :account/email (fu/valid-email? v)
      :account/password (fu/valid-password? v)
      :account/password-again (= (:account/password form) v))))

(def validator (fs/make-validator signup-valid))

(def ui-google-login (react-factory GoogleLogin))

(defsc Signup [this {:account/keys [email password password-again] :as props}]
  {:query             [:account/email :account/password :account/password-again fs/form-config-join session-join
                       [df/marker-table ::signup]]
   :initial-state     (fn [_]
                        (fs/add-form-config Signup
                                            {:account/email          ""
                                             :account/password       ""
                                             :account/password-again ""}))
   :form-fields       #{:account/email :account/password :account/password-again}
   :ident             (fn [] signup-ident)
   ;; :route-segment     ["signup"]
   ;; ::fr/route         ["/signup" {:name :signup :segment ["signup"]}]
   :componentDidMount (fn [this] (comp/transact! this [(clear-signup-form)]))}
  (let [server-err     (:session/server-error-msg (get-session props))
        form-valid?    (= :valid (validator props))
        submit!        (fu/prevent-default
                        #(when form-valid?
                           (comp/transact! this [(signup {:password password :email email})
                                                 ])))
        auth-google!   (fn [jwt]
                         (fu/prevent-default
                          (comp/transact! this [(auth-google {:jwt jwt})])))
        checked?       (fs/checked? props)
        mark-complete! (partial mark-complete! this)
        saving?        (df/loading? (get props [df/marker-table ::signup]))]
    (useGoogleOneTapLogin
     #js {:onSuccess   #(auth-google! (.-credential %))
          :onError     #(js/console.log "error" %)
          :useOneTap   true
          :auto_select true
          :shape       "pill"
          :login_url   "http://localhost:8085/api/google-auth"})
    #_
    [:form
     {:class    (str "ui form" (when checked? " error"))
      :onSubmit submit!}
     ^:inline (fu/ui-email this :account/email email mark-complete! :autofocus? true
                           :tabIndex 1)
     ^:inline (fu/ui-password2 this :account/password password :tabIndex 2)
     ^:inline (fu/ui-verify-password this :account/password-again
                                     password password-again mark-complete!
                                     :tabIndex 3)
     (when-not (empty? server-err) [:div.ui.error.message server-err])
     [:button
      {:type      "submit"
       :tab-index 4
       :class     (str "ui primary button" (when saving? " loading"))
       :disabled  (not form-valid?)} "Sign Up"]]))

(def ui-signup (c/factory Signup))

(defsc GoogleAuth [this props]
  {:use-hooks? true}
  )

(def ui-google-auth (c/factory GoogleAuth))

(defsc Profile [this {:keys [profile/session]}]
  {:query         [{:profile/session (comp/get-query Session)}
                   [::sm/asm-id ::session/session]]
   :initial-state (fn [_] {:profile/session (comp/get-initial-state Session)})
   :ident         (fn [] [:component/id :profile])
   :use-hooks?    true}
  (let [auth-google! (fn [jwt]
                       (fu/prevent-default
                        (comp/transact! this [(auth-google {:jwt jwt})])))]
    (dom/div
     {:style {:height "50px" :minHeight "50px" :maxHeight "50px"}}
     (if (:session/valid? session)
       (ui-button
        #js {:kind          "secondary"
             :onClick       #(do (js/console.log "logging out")
                                 (googleLogout)
                                 (sm/trigger! this ::session/session :event/logout))
             :shape         "pill"
             :startEnhancer #(when-let [picture (:user/picture session)]
                               (ui-avatar
                                #js {;;:name (:user/name session)
                                     :size "scale600"
                                     :src  picture}))}
        "Logout")

       (ui-google-login
        #js {:onSuccess    #(auth-google! (.-credential %))
             :onError      #(js/console.log "error" %)
             "useOneTap"   true
             "ui_mode"     "popup"
             "itp_support" true
             "prompt"      "none"
             "disabled"    (some? (:user/email session))
             ;; :auto_select true
             ;; :shape     "pill"
             :login_url    "http://localhost:8085/api/google-auth"})
       ))))

(def ui-profile (c/factory Profile))

(defmutation add-profile [profile]
  (action [{:keys [state]}]
          (js/console.log "session" profile)
          (swap! state merge/merge-component Profile profile)))
