# scan-img

A web application based on ClojureScript and Clojure for execution of remote commands

- ClojureScript for JavaScript UI,
- Clojure for backend
- [re-frame](https://github.com/Day8/re-frame)
- Reagent for reactive functionality
- Ring for HTTP abstraction
- Compojure for routing
- core.async for CSP in he file handling service

## Configuration

Requires a config.edn with commands to execute. Otherwise commands can be defined in a file in EDN format and uploaded. The place-holder {{canonical-path}} is is replaced by the path where the file is stored. That allows to perform actions on an uploaded file. E.g. import it into Docker.

```clojure
{:name "xyz"
 :port 3000
 :mode :dev
 :executable-cmd [["ls" "-al" "{{cannonical-path}}"]
                  ["java" "-version"]
                  ["lein" "version"]
                  ["uname" "-a"]]}
```

If the file is uploaded as a command file then the commands will be executed.


## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

```
lein clean
lein with-profile prod uberjar
```

That should compile the ClojureScript code first, and then create the standalone jar.

When you run the jar you can set the port the ring server will use by setting the environment variable PORT.
If it's not set, it will run on port 3000 by default.

To compile ClojureScript to JavaScript:

```
lein clean
lein cljsbuild once min
```
