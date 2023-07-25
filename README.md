# Prerequisites

This template utilizes GNU Make 4.x. You'll need to install it first 
before executing `make`.

This template uses `yarn` to handle npm dependencies.
You'll also need to have Clojure installed. See [how to](https://clojure.org/guides/install_clojure).

# Architecture
## ClojureScript SPA with Fulcro
[Fulcro](https://github.com/fulcrologic/fulcro) is a full-stack framework.
It uses ReactJS for render, providing handy dev interface to ease development.
Fulcro component is akin to ReactJS component augmented with:
1. query description
   It describes what data component needs.
   It's in [EQL](https://book.fulcrologic.com/#_queries_eql), akin to GraphQL.
   As our SPA is a tree of components, each component describes data needed to render itself (and its downstream children).
   To populate component with data we can simply take it's query description and run agains the backend.
   This can be used in SSR as well.
   See [docs](https://book.fulcrologic.com/#_the_glue_ui_components) for more.
2. initial state
   In order to first render component it needs some initial state.
   Instead of keeping it in DB and trying to have it in sync with components, which is error prone,
   we have initial state co-located in components.
   On start-up of the app we simply take initial state of the root component - this provides us with all the necessary data for initial render.
   Also can be used in SSR.
   See [docs](https://book.fulcrologic.com/#_step_1the_initial_state) for more.
3. routing
   Instead of keeping a separate route table from paths to pages we co-located routing information in components.
   This allows to have uniform routing at page-level and sub-page level (e.g., switching tabs within a page).
   Route switching can be done via href links, which grants us the ability to have deep links.
   [reitit](https://github.com/metosin/reitit) is used for path matching.
   It can be used for routing within SPA, SSR and backend API.
   See [docs](https://book.fulcrologic.com/#_dynamic_router) for more.

Overall, Fulcro components pack all the information needed for them to be a part of the app.
Fulcro comes with great support for form management, state machines.
On top of it we can have handy dev tools, such as [workspaces](https://github.com/nubank/workspaces) and [devcards](https://github.com/bhauman/devcards).
Also we can use [malli](https://github.com/metosin/malli) for when we need ala JSON Schema, e.g., for form validation, route params validation and coercion, generative testing.

UI components can be written in [function style](https://book.fulcrologic.com/#_basic_ui_components)
```clojure
(div {}
  (h1 {} "Header")
  (button {:onClick (fn [event] ...)} "Click Me!"))
```
or in [Hiccup style](https://github.com/reagent-project/reagent#examples), via plain data structures
```clojure
[:div
  [:h1 "Header"]
  [:button {:onClick (fn [event] ...)} "Click Me!"]]
```
And it can be mixed-and-matched as your soul likes
```clojure
[:div
  [:h1 "Header"]
  (button {:onClick (fn [event] ...)} "Click Me!")]
```

### BaseWeb components library
SPA makes use of [BaseWeb](https://baseweb.design/) components library.

### Auth
Google auth is handled by [react-oauth](https://github.com/MomenSherif/react-oauth).
SPA obtains JWT and sends it to our API to obtain a session with us.
Session is tracked by a cookie, and is restored on subsequent visits to the site, if not expired.

## Clojure backend API
Clojure backend API simply serves a Pathom resolver, wrapped with auth.
SPA obtains a session by sending a EQL mutation (if it wasn't restored by SSR already).
And then sends EQL queries and mutations. So EQL endpoint is akin to GraphQL endpoint.
To handle EQL we use [Pathom](https://github.com/wilkerlucio/pathom).
As a side-bonus, Pathom can run in ClojureScript, so we can make use of it from within our SPA, e.g., to derive data out of app db, if we fancy.

## Clojure backend DB
XTDB v1 is used as the database.
It allows us to build flexible graph models and use powerful Datalog for graph querying.

# Start dev

In dev environment we have 3 servers:
1. shadow-cljs server to watch and build ClojureScript app and development tools (workspaces and devcards).
2. Clojure server to serve full SPA and API.
3. ClojureScript server to serve SSR SPA.

## Editor-less setup
### shadow-cljs server

In one terminal:
```bash
make
```
this runs `yarn install` and starts [the shadow-cljs watch processes](https://shadow-cljs.github.io/docs/UsersGuide.html#_server_mode).

Please see the `shadow-cljs.edn` file for ports used for development builds.

If any of those ports are used already shadow-cljs will try different ports so please see the console output 
by shadow-cljs.

When the main build is complete, start the backend server either in an editor or at the command line.

The clj server reads the manifest file produced by shadow-cljs so the build must complete before you start the server.

### Clojure server
To stars the backend without editor, run
```bash
clojure -M:dev:guardrails
```
It'll start a terminal-based repl for you, where you can execute code, including starting the backend via
```clojure
(start)
```
This uses [mount](https://github.com/tolitius/mount) to start the web server.
And when you make changes to backend codebase, restart it via
```clojure
(restart)
```

The clojure webserver listens on port 8085 by default - this is specified in `src/main/config/defaults.edn`
http://localhost:8085

You can open it in browser to load the SPA.
However, it won't serve the full SPA and not the SSR one.
This is because it runs on JVM and it's not possible to SSR JS components there.
This server is used to serve API.

### ClojureScript server
To serve SSR app, run a ClojureScript web server, that runs on NodeJS, via
```bash
./scripts/start_node_server.sh
```

This server runs ClojureScript on top of NodeJS and serves SSR version of the app.
Running on NodeJS allows us to SSR JS components.
It uses Clojure server's API, that runs on JVM.

## Editor setup
To have the best dev experience you can connect your editor to the servers and eval code from within your editor and have other run-time enabled editor features.

To do, in your editor:
add 2 repls:

### frontend repl:

nREPL remote:

  localhost:$port
  
The $port defaults to 9000 but may be different if 9000 is already in use.

Using this repl you connect to the various ClojureScript builds using `(shadow/repl :build-id)`
E.g., connect to ClojureScript repl using:

```clojure
(shadow/repl :node-server)
```

### backend repl:

nREPL local

enable these deps aliases: dev, test, guardrails

start Clojure server repl with editor support:
```shell
clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "1.0.0"} cider/cider-nrepl {:mvn/version "0.29.0"}} :aliases {:cider/nrepl {:main-opts ["-m" "nrepl.cmdline" "--middleware" "[cider.nrepl/cider-middleware]"]}}}' -M:dev:guardrails:cider/nrepl
```

then:
```clojure
(start) ;; (user/start)
```

_note_ you do not need to specify any JVM parameters.

# Production server build

All builds are handled by tasks in the Makefile.

Both frontend and backend builds:
```bash
make prod-build
```

Server jar only:

```bash
make be-release
```

Run the prod server:
```bash
make start-prod-server
```

# Workspaces
[Workspaces](https://github.com/awkay/workspaces) are available at:

http://127.0.0.1:8023

Again, the port may be different if 8023 is already in use.

# Devcards
[Devcards](https://github.com/bhauman/devcards) are available at:

http://127.0.0.1:4001
