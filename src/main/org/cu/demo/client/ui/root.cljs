(ns org.cu.demo.client.ui.root
  (:require
   [reagent.core :as re]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   [com.fulcrologic.fulcro.components :as c :refer [defsc force-children]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div]]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.ui-state-machines :as sm]
   [dv.fulcro-reitit :as fr]
   [dv.cljs-emotion-reagent :refer [global-style theme-provider]]
   [org.cu.demo.baseui :refer [ui-base-provider
                               ui-theme-provider light-theme dark-theme
                               ui-navbar ui-navbar-item
                               ui-header-nav ui-header-nav-list ui-header-nav-item header-nav-list-align
                               ui-block
                               ]]
   [org.cu.demo.task.ui.task-page :refer [TaskPage]]
   [org.cu.demo.client.application :refer [SPA]]
   [org.cu.demo.client.ui.styles.app-styles :as styles]
   [org.cu.demo.client.ui.styles.global-styles :refer [global-styles]]
   [org.cu.demo.client.ui.styles.style-themes :as themes]
   [org.cu.demo.auth.login :refer [ui-login Login Session session-join valid-session?]]
   [org.cu.demo.auth.signup :refer [Signup ui-profile Profile]]
   [org.cu.demo.client.ui.root-utils :refer [ui-styletron styletron-engine]]
   [taoensso.timbre :as log :refer [spy]]
   [reitit.core]
   [reitit.frontend.easy]

   ;; ["styletron-client" :as StyletronClient]
   ;; ["styletron-server" :refer []]
   ["@react-oauth/google" :refer [GoogleOAuthProvider]]
   ))

(dr/defrouter TopRouter
  [this {:keys [current-state route-factory route-props]}]
  {:router-targets [TaskPage]})

(def ui-top-router (c/factory TopRouter))

(defn menu [{:keys [session? login]}]
  [:div.ui.secondary.pointing.menu
   (concat
    (map (fn [p] ^{:key (name p)} [:a.item #_{:href (fr/route-href (spy {:PPPP p}))} (name p)])
         (if session? [:default :tasks] [:default]))
    [(ui-login login)])])

(defsc PageContainer [this {:root/keys [router login] :as props}]
  {:query         [{:root/router (c/get-query TopRouter)}
                   [::sm/asm-id ::TopRouter]
                   session-join
                   {:root/login (c/get-query Login)}]
   :ident         (fn [] [:component/id :page-container])
   :initial-state (fn [_] {:root/router             (c/get-initial-state TopRouter {})
                           :root/login              (c/get-initial-state Login {})
                           :root/signup             (c/get-initial-state Signup {})
                           [:component/id :session] (c/get-initial-state Session {})})}
  (let [#_#_current-tab (fr/current-route this)
        session?        (valid-session? props)]
    [:div.ui.container
     #_(menu {:session? session? :login login})
     (ui-top-router router)]))

(fr/register-fulcro-router! SPA TopRouter)
;; (with-redefs [reitit.frontend.easy/start! (fn [router on-navigate ops]
;;                                             (reitit.core/router router ops))]
;;   (fr/start-router! SPA)) ;; depends on window history
;; (fr/start-router! SPA)
(def ui-page-container (c/factory PageContainer))
;; (def styletron-engine (new Client))
;; (def styletron (interop/react-factory Provider))
(def ui-google-oauth-provider (interop/react-factory GoogleOAuthProvider))

(defsc Root [this {:root/keys [page-container style-theme profile]}]
  {:query         [{:root/page-container (c/get-query PageContainer)}
                   :root/style-theme
                   {:root/profile (c/get-query Profile)}]
   :initial-state (fn [_] {:root/page-container (c/get-initial-state PageContainer {})
                           :root/style-theme    themes/light-theme
                           :root/profile        (c/get-initial-state Profile {})})}
  (log/info "ROOT RENDER")
  #_(theme-provider
     {:theme style-theme}
     (global-style (global-styles style-theme)))
  #_(ui-base-provider #js {"theme" light-theme}
                      (force-children (div {:id "root"}
                                           (ui-page-container page-container))))
  (js/console.log "profile" profile)
  (div {:id "root"}
       (ui-google-oauth-provider
        #js {:clientId "337188929288-r8g1k8rldneitr7v8e80mels09m0v21j.apps.googleusercontent.com"}
        (ui-styletron
         #js {"value" styletron-engine}
         (ui-base-provider
          #js {"theme" light-theme}
          (ui-header-nav
           #{}
           (ui-header-nav-list
            #js {}
            (ui-header-nav-item
             #js {}
             "Task Tracker"))
           (ui-header-nav-list
            #js {"$align" (.-right header-nav-list-align)}
            (ui-header-nav-item
             #js {}
             (ui-profile profile))))
          (dom/div {:style {:margin-left "25vw"
                            :width       "50vw"
                            :margin-top  "20px"}}
                   (ui-page-container page-container)))))))

(def ui-root (c/factory Root))

(defsc RootSSR [this {:root-ssr/keys [root]}]
  {:query         [{:root-ssr/root (c/get-query Root)}]
   :initial-state (fn [_] {:root-ssr/root (c/get-initial-state Root {})})}
  (ui-root root))

(defsc RootSSRHydrate [this {:root-ssr/keys [root]}]
  {:query         [{:root-ssr/root (c/get-query Root)}]
   :initial-state (fn [_] {:root-ssr/root (c/get-initial-state Root {})})}
  (ui-root root)
  #_(let [styletron-engine (new Client)
          styletron        (new Provider)]
      (styletron #js {"value" styletron-engine}
                 (ui-base-provider #js {"theme" light-theme}
                                   (ui-root root)))))
