#!/bin/bash -

set -euo pipefail

main() {
  echo npx yarn install
  npx yarn install

  echo '
  Greetings. I trust you will have an excellent day.

  Starting shadow-cljs watches for you via the following command:

  npx yarn run shadow-cljs watch main node-server test devcards workspaces

  # shadow-cljs builds:
  http://localhost:9630

  # Frontend app
  start nrepl to shadow-cljs port (see shadow-cljs.edn) then connect with:
  (shadow/repl :main)

  # Backend
  1. Start clj repl for backend in your editor and then execute:
  (user/start)

  # Tests
 http://localhost:8022

 # Workspaces
 http://localhost:8023
'

  npx yarn run shadow-cljs watch main node-server test devcards workspaces
}

main "$@"
