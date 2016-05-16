(ns plomber.keyboard
  (:require [goog.events :as gevt]
            [om.next :as om]))

(def code->key
  "map from a character code (read from events with event.which)
  to a string representation of it.
  Only need to add 'special' things here."
  {  8 "backspace"
    13 "enter"
    16 "shift"
    17 "ctrl"
    18 "alt"
    27 "esc"
    33 "pageup"
    34 "pagedown"
    36 "home"
    37 "left"
    38 "up"
    39 "right"
    40 "down"
    46 "del"
    91 "meta"
    32 "space"
   186 ";"
   191 "/"
   219 "["
   221 "]"
   187 "="
   189 "-"
   190 "."
   220 "\\"})

(def modifier-translation
  {"shiftKey" "shift"
   "altKey"   "alt"
   "ctrlKey"  "ctrl"
   "metaKey"  "meta"})

(defn event-modifiers
  "Given a keydown event, return the set of modifier keys that were being held."
  [e]
  (reduce (fn [ret [modifier key-name]]
            (if (aget e modifier)
              (conj ret key-name)
              ret))
    #{} modifier-translation))

(defn event->key-set
  "Given an event, return a set of keys string like #{\"up\"} or #{\"shift\" \"l\"}
  describing the keys that were pressed. Will return lone modifier keys, like shift or ctrl"
  [e]
  (let [code (.-keyCode e)
        key (or (code->key code) (.toLowerCase (js/String.fromCharCode code)))]
    (conj (event-modifiers e) key)))

(defn match-keys
  "Given a keymap for the component and the most recent series of keys
  that were pressed (not the codes, but sets of keys like #{'shift' 'r'})
  return a handler fn associated with a key combo in the keys
  list or nil."
  [keymap keys]
  (->> keymap
    (keep (fn [[key-set f]]
            (when (= keys key-set) f)))
    first))

(defn register-key-handler [component keymap]
  (om/react-set-state! component
    {::event-key
     (goog.events/listen
       js/window
       "keydown"
       (fn [e]
         (when-let [f (match-keys keymap (event->key-set e))]
           (f))))}))

(defn dispose-key-handler [component]
  (goog.events/unlistenByKey (get (om/get-rendered-state component) ::event-key)))
