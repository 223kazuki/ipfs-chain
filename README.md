# ipfs-chain

A IPFS SPA using [re-integrant pattern](https://223kazuki.github.io/re-integrant-app.html).

Hosted at https://ipfs.infura.io/ipfs/Qmbqap913AY77BNX2aXGUpU7Q3Vmguu85KZQ3q6KTzGkXd

## Development Mode:

### Start Cider from Emacs:

Put this in your Emacs config file:

```
(setq cider-cljs-lein-repl
    "(do (require 'figwheel-sidecar.repl-api)
         (figwheel-sidecar.repl-api/start-figwheel!)
         (figwheel-sidecar.repl-api/cljs-repl))")
```

Navigate to a clojurescript file and start a figwheel REPL with `cider-jack-in-clojurescript` or (`C-c M-J`)

### Run application:

```
lein dev
```

Figwheel will automatically push cljs changes to the browser.  
Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

### Run tests:

```
lein clean
lein doo phantom test once
```

The above command assumes that you have [phantomjs](https://www.npmjs.com/package/phantomjs) installed. However, please note that [doo](https://github.com/bensu/doo) can be configured to run cljs.test in many other JS environments (chrome, ie, safari, opera, slimer, node, rhino, or nashorn).

## Production Build:

```
lein do clean, build
```
