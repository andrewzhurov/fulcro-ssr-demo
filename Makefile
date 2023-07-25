SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

server-jar-file := server.jar
node-server-file := node-server.js
fe-module := main

ifeq ($(origin .RECIPEPREFIX), undefined)
  $(error This Make does not support .RECIPEPREFIX. Please use GNU Make 4.0 or later)
endif
.RECIPEPREFIX = >

shadow-server:
> npx yarn
> bash ./scripts/start_dev.sh

fe:
> bash ./scripts/start_dev.sh

prod-build: fe-release be-release node-server-release

shadow-report:
> npx yarn shadow-cljs run shadow.cljs.build-report $(fe-module) fe-bundle-report.html

watch-$(fe-module):
> npx yarn shadow-cljs watch :$(fe-module)

watch: watch-$(fe-module)
watch-workspaces:
> npx yarn shadow-cljs watch :workspaces
watch-devcards:
> npx yarn shadow-cljs watch :devcards
watch-client-test:
> npx yarn shadow-cljs watch :test

fe-test:
> npx yarn shadow-cljs compile ci-tests
> npx yarn karma start --single-run
> clj -A:dev:clj-tests
watch-all: watch-$(fe-module)watch-workspaceswatch-client-test

fe-release:
> npx yarn shadow-cljs release $(fe-module)
builds/$(node-server-file):
> npx yarn shadow-cljs release node-server

node-server-release: builds/$(node-server-file)
deploy/$(server-jar-file):
> clojure -A:depstar -m hf.depstar.uberjar deploy/$(server-jar-file)

be-release: deploy/$(server-jar-file)

clean:
> rm deploy/$(server-jar-file)
> rm builds/node-server/$(node-server-file)

be-repl:
> clj -A:dev:test:guardrails

start-prod-server:
> pushd deploy
> java -cp $(server-jar-file) clojure.main -m org.cu.demo.server.server-entry

.PHONY: fe fe-release prod-build shadow-report watch-$(fe-module) watch shadow-server
.PHONY: watch-workspaces
.PHONY: fe-test watch-client-test
.PHONY: be-release clean start-dev-server start-prod-server
