(ns org.cu.demo.client.ui.root-utils
  #?(:node (:require ["styletron-engine-atomic" :refer [Server] :rename {Server StyletronEngine}]
                     ["styletron-react" :refer [Provider]]
                     [com.fulcrologic.fulcro.algorithms.react-interop :as interop])
     :cljs (:require ["styletron-engine-atomic" :refer [Client] :rename {Client StyletronEngine}]
                     ["styletron-react" :refer [Provider]]
                     [com.fulcrologic.fulcro.algorithms.react-interop :as interop])))

#?(:cljs (def ui-styletron (interop/react-factory Provider))
   :node (def ui-styletron (interop/react-factory Provider)))

#?(:cljs (def styletron-engine (new StyletronEngine))
   :node (def styletron-engine (new StyletronEngine)))
