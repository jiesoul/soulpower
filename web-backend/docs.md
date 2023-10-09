# Docs

web backend server include private api and public api.

## lib

* [Ring](https://github.com/ring-clojure/ring) web service
* [Intgrant](https://github.com/weavejester/integrant) DI
* [Reitit](https://github.com/metosin/reitit) API Routes

## Run

### dev

```clojure
clj -M:dev:test 

clj꞉user꞉> (go)
:initiated

```

### prod

```clojure
clj -T:build uber

java -jar .\target\xxxxx-1.0.13.jar prod

```
