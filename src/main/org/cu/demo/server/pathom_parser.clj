(ns org.cu.demo.server.pathom-parser
  (:require
    [com.wsscode.pathom.connect :as pc]
    [dv.pathom :refer [build-parser]]
    [mount.core :refer [defstate]]
    [taoensso.timbre :as log]
    [org.cu.demo.task.task-resolvers :as task]
    [org.cu.demo.auth.session :as session]
    [org.cu.demo.auth.user :as user]
    [org.cu.demo.server.config :refer [config]]
    [org.cu.demo.server.xtdb-node :refer [xtdb-node]]))

(def all-resolvers
  [session/resolvers
   user/resolvers
   task/resolvers])

(defstate parser
  :start
  (let [{:keys [log-responses? trace? index-explorer? connect-viz?]} (::config config)]
    (log/info "Constructing pathom parser with config: " (::config config))
    (build-parser
      {:resolvers          all-resolvers
       :log-responses?     log-responses?
       :handle-errors?     true
       :trace?             trace?
       :index-explorer?    index-explorer?
       :enable-pathom-viz? connect-viz?
       :env-additions      (fn [env]
                             {:xtdb-node    xtdb-node
                              :config       config
                              :current-user (user/get-current-user env)})})))

(comment
  (parser {} [{:all-tasks [:task/id :task/description]}])
  )
