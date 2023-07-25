(ns org.cu.demo.client.client-entry
  (:require
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.ui-state-machines :as uism]
   [com.fulcrologic.fulcro.components :as c]
   [clojure.edn :as edn]
   [org.cu.demo.client.ui.root :as root]
   [org.cu.demo.client.application :refer [SPA]]
   [org.cu.demo.client.malli-registry :as reg]
   [org.cu.demo.auth.login :refer [Login Session]]
   [org.cu.demo.auth.session :as session]

   [dv.fulcro-reitit :as fr]
   [reagent.core :as re]
   [reagent.dom.server :as rdom]
   [com.fulcrologic.fulcro.components :as comp :refer [*app* defsc]]
   [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.algorithms.server-render :as ssr]
   [reitit.core :as r]

   [malli.registry :as mr]
   [shadow.resource :as rc]
   [taoensso.timbre :as log :refer [spy]]))

;; set logging lvl using goog-define, see shadow-cljs.edn
(goog-define LOG_LEVEL "warn")

(def fe-config (edn/read-string (rc/inline "/config/fe-config.edn")))
(log/info "Log level is: " LOG_LEVEL)

(def log-config
  (merge
    (-> fe-config ::config :logging)
    {:level (keyword LOG_LEVEL)}))

(defn ^:export refresh []
  (log/info "Hot code Remount")
  (log/merge-config! log-config)
  (c/refresh-dynamic-queries! SPA)
  (app/mount! SPA root/Root "app"))

(defn route! [app]
  (let [router        (fr/router-state app :reitit-router)
        routes        (fr/router-state app :routes-by-name)
        app-url       (-> js/window .-location .-href (.replace (-> js/window .-location .-origin) ""))
        m             (spy (r/match-by-path router app-url))
        route-segment (-> m :data :name routes :segment)]
    (js/console.log "app url" app-url)
    (js/console.log "router" router)
    (js/console.log "routes" routes)
    (js/console.log "match" m)
    (js/console.log "route-segment" route-segment)
    (dr/change-route! app route-segment)))

(defn start []
  (log/merge-config! log-config)
  (log/info "Starting from scratch.")
  (log/info "Application starting.")
  (app/set-root! SPA root/Root {:initialize-state? true})
  (fr/start-router! SPA)
  (route! SPA)
  ;; TODO navigate to signup

  (log/info "Starting session machine.")
  (uism/begin! SPA session/session-machine ::session/session
               {:actor/login-form      Login
                :actor/current-session Session})

  (log/info "MOUNTING APP")
  (js/setTimeout #(app/mount! SPA root/Root "app" {:initialize-state? true})))

(defn start-from-ssr []
  ;; (app/set-root! SPA root/Root {:initialize-state? false})
  (log/info "Starting from SSR")

  (log/info "Starting session machine.")
  (uism/begin! SPA session/session-machine ::session/session
               {:actor/login-form      Login
                :actor/current-session Session})

  ;; Restore initial state and route
  (let [db (ssr/get-SSR-initial-state)]
    (js/console.log "SSR DB:" db)
    (reset! (::app/state-atom SPA) db))
  (app/set-root! SPA root/RootSSR {:initialize-state? false})
  (dr/initialize! SPA)
  (route! SPA)
  #_(dr/change-route! SPA ["signup"])

  (log/info "MOUNTING APP")
  (js/setTimeout #(app/mount! SPA root/RootSSR "app" {:hydrate? true :initialize-state? false}) 0))

(defn ^:export init []
  (log/merge-config! log-config)
  (log/info "Application starting.")
  (mr/set-default-registry! (mr/mutable-registry reg/registry*))
  (if js/window.SSR
    (start-from-ssr)
    (start)))
