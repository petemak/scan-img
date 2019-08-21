# scan-img

A web application based on ClojureScript and Clojuer designed to well, that part is up to you.

- ClojureSctript for JavaScript UI,
- Clojure for backend
- [re-frame](https://github.com/Day8/re-frame)
- Reagent for reactive functionality
- Ring for HTTP abstraction
- Compjure for routing
- core.async for CSP in he file handling service


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

That should compile the clojurescript code first, and then create the standalone jar.

When you run the jar you can set the port the ring server will use by setting the environment variable PORT.
If it's not set, it will run on port 3000 by default.


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
