# Plomber [![CircleCI](https://circleci.com/gh/anmonteiro/plomber.svg?style=svg&circle-token=c36d14af28ad66d21060d4fbdc520151b0b027d7)](https://circleci.com/gh/anmonteiro/plomber)

Component instrumentation for Om Next.

![](https://cloud.githubusercontent.com/assets/661909/15430238/bb54dc40-1ea4-11e6-81b8-239969d3dc18.gif)

## Contents

- [Installation](#installation)
- [Guide](#guide)
  - [Getting started](#getting-started)
  - [Instrumenting an Om Next application](#instrumenting-an-om-next-application)
  - [Customization & Extras](#customization--extras)
- [Copyright & License](#copyright--license)

## Installation

- Requires at least Om 1.0.0-alpha40

Not deployed. Let me know if you want to use it.

## Guide

Plomber is heavily inspired by [PrecursorApp/om-i](https://github.com/PrecursorApp/om-i), and makes possible to identify which components in your Om Next application are rendering unnecessarily. It lists statistics pertaining to the mounting / rendering behavior of the components in your application, such as, but not limited to, the time it took to last render a component, its best / worst performing render, etc.

### Getting started

To get started, require Plomber where you declare the Om Next reconciler in your application.

```clojure
(ns my-app.core
  (:require [plomber.core :as plomber]
            [om.next :as om]))
```

### Instrumenting an Om Next application

Setting up instrumentation for your Om Next application is easy with Plomber. Simply add an `:instrument` key to your Om Next reconciler as shown below.

```clojure
(def reconciler
  (om/reconciler {:state ...
                  :parser ...
                  ;; ADD THIS:
                  :instrument (plomber/instrument)}))
```

To toggle the instrumentation panel, click <kbd>Ctrl+Shift+s</kbd>. You can clear the list by clicking <kbd>Ctrl+Shift+k</kbd>. Sorting the metrics by your favorite attribute is as easy as clicking the column heading for a given statistic.

### Customization & Extras

Plomber allows you to provide a map of options to the `plumber.core/instrument` function to customize its behavior. Currently, the following are supported:

- `keymap` — a map which can contain the keywords `toggle-shortcut` and `clear-shortcut` with the corresponding keymaps as values. A full example is shown below;
- `extra-fn` — an extra function that Plomber calls for each component it instruments. This is a function of 1 argument, which receives a map with keys `factory`, `class`, `props` and `children`, corresponding to the Om Next component's factory, class, props and children, respectively.


Customization example:

```clojure
(def reconciler
  (om/reconciler {:state ...
                  :parser ...
                  ;; change :toggle-shortcut to `Ctrl+Shift+t`
                  :instrument (plomber/instrument
                                {:keymap {:toggle-shortcut #{"ctrl" "shift" "t"}}
                                 :extra-fn (fn [{:keys [class factory props children]}]
                                             (println "instrumenting component" class))})}))
```

## Copyright & License

Copyright © 2016 António Nuno Monteiro

Distributed under the Eclipse Public License either version 1.0.

Contains code adapted from the following projects:

- [PrecursorApp/om-i](https://github.com/PrecursorApp/om-i)
