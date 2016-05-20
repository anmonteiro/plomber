(ns plomber.core
  (:require-macros [plomber.core :refer [wrap-lifecycle-methods show-when]])
  (:require [goog.dom :as gdom]
            [goog.object :as gobj]
            [goog.string :as gstr]
            [goog.string.format]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [plomber.keyboard :as kbd]))

(defmulti update-stats
  (fn [_ {:keys [type]}]
    type))

(defmethod update-stats :default
  [state {:keys [type when component]}]
  (swap! state update-in [:measurements component]
    merge {type when}))

(defmethod update-stats :did-mount
  [state {:keys [type when component]}]
  (swap! state update-in [:measurements component]
    (fn [stats]
      (-> stats
        (assoc type when)
        (update-in [:mount-ts] (fnil conj [])
          (max (- when (get stats :will-mount when)) 0))))))

(defmethod update-stats :did-update
  [state {:keys [type when component]}]
  (swap! state update-in [:measurements component]
    (fn [stats]
      (-> stats
        (assoc type when)
        (update-in [:render-ts] (fnil conj [])
          (max (- when (get stats :will-update when)) 0))))))

(defn display-name [c]
  (.. c -constructor -displayName))

(defn wrap-componentWillMount [state f]
  (fn [next-props next-state]
    (this-as this
      (update-stats state {:type :will-mount
                           :component (display-name this)
                           :when (system-time)})
      (.call f this))))

(defn wrap-componentDidMount [state f]
  (fn [next-props next-state]
    (this-as this
      (update-stats state {:type :did-mount
                           :component (display-name this)
                           :when (system-time)})
      (when f
        (.call f this)))))

(defn wrap-componentWillUpdate [state f]
  (fn [next-props next-state]
    (this-as this
      (update-stats state {:type :will-update
                           :component (display-name this)
                           :when (system-time)})
      (.call f this next-props next-state))))

(defn wrap-componentDidUpdate [state f]
  (fn [next-props next-state]
    (this-as this
      (update-stats state {:type :did-update
                           :component (display-name this)
                           :when (system-time)})
      (.call f this next-props next-state))))

(defn avg [coll]
  (/ (reduce + coll)
    (count coll)))

(defn std-dev [coll]
  (let [a (avg coll)]
    (Math/sqrt (avg (map #(Math/pow (- % a) 2) coll)))))

(defn generate-stats [{:keys [measurements sort-key sort-asc?]}]
  (let [stats (reduce
                (fn [ret [name samples]]
                  (let [{:keys [mount-ts render-ts]} samples
                        mount-count (count mount-ts)
                        render-count (count render-ts)
                        stat (merge {:component-name name
                                     :mount-count mount-count
                                     :render-count render-count}
                               (when (pos? mount-count)
                                 {:max-mount-ms (apply max mount-ts)
                                  :min-mount-ms (apply min mount-ts) :mount-std-dev (std-dev mount-ts)
                                  :last-mount-ms (peek mount-ts) :avg-mount-ms (avg mount-ts)})
                               (when (pos? render-count)
                                 {:max-render-ms (apply max render-ts)
                                  :min-render-ms (apply min render-ts)
                                  :render-std-dev (std-dev render-ts) :last-render-ms (peek render-ts)
                                  :avg-render-ms (avg render-ts)}))]
                    (conj ret stat))) [] measurements)
        sort? (some sort-key stats)]
    (cond->> stats
      sort? (sort-by sort-key)
      (and sort?
           (not sort-asc?)) reverse)))

(defui StatsRow
  Object
  (shouldComponentUpdate [this next-props _]
    (let [{render-count' :render-count
           mount-count' :mount-count} (-> next-props
                                          (gobj/get "omcljs$value")
                                          om/unwrap)
          {:keys [render-count mount-count]} (om/props this)]
      (not (and (== render-count render-count')
                (== mount-count mount-count')))))
  (render [this]
    (let [{:keys [component-name render-count mount-count last-render-ms
                  last-mount-ms avg-render-ms avg-mount-ms max-render-ms
                  max-mount-ms min-render-ms min-mount-ms render-std-dev
                  mount-std-dev]} (om/props this)
          format-ms (partial gstr/format "%.2f ms")
          format-% (partial gstr/format "%.2f %")]
      (dom/tr nil
        (dom/td nil component-name)
        (dom/td #js {:className "number"} render-count)
        (dom/td #js {:className "number"} mount-count)

        (dom/td #js {:className "number"} (show-when last-render-ms format-ms))
        (dom/td #js {:className "number"} (show-when last-mount-ms format-ms))

        (dom/td #js {:className "number"} (show-when avg-render-ms format-ms))
        (dom/td #js {:className "number"} (show-when avg-mount-ms format-ms))

        (dom/td #js {:className "number"} (show-when max-render-ms format-ms))
        (dom/td #js {:className "number"} (show-when max-mount-ms format-ms))

        (dom/td #js {:className "number"} (show-when min-render-ms format-ms))
        (dom/td #js {:className "number"} (show-when min-mount-ms format-ms))

        (dom/td #js {:className "number"} (show-when render-std-dev format-%))
        (dom/td #js {:className "number"} (show-when mount-std-dev format-%))))))

(def stats-row (om/factory StatsRow))

(defn- compute-label [base-label sort? sort-asc?]
  (cond-> base-label
    sort? (str (if sort-asc?
                 (gstr/unescapeEntities " &#8679;")
                 (gstr/unescapeEntities " &#8681;")))))

(defn- handle-th-click [c sort-k e]
  (let [state (om/app-state (om/get-reconciler c))
        {:keys [sort-key sort-asc?]} @state]
    (swap! state merge
      {:sort-key sort-k
       :sort-asc? (if (keyword-identical? sort-key sort-k)
                    (not sort-asc?)
                    true)})))

(defn- stats-table-header
  [c label key props]
  (dom/th props
    (dom/span nil label)))

(defn- render-table-headers [c]
  (let [{:keys [sort-key sort-asc?] :as props} (om/props c)]
    (dom/tr nil
      (map (fn [[label sort-k props]]
             (let [props (if props
                           props
                           #js {:colSpan 2
                                :key label
                                :onClick #(handle-th-click c sort-k %)})]
               (stats-table-header c label sort-k props)))
        [[(compute-label "Component"
            (keyword-identical? sort-key :component-name) sort-asc?) :component-name
          #js {:key "component-name" :onClick #(handle-th-click c :component-name %)}]
         [(compute-label "#" (.endsWith (name sort-key) "count") sort-asc?) :render-count]
         [(compute-label "Last" (.startsWith (name sort-key) "last") sort-asc?) :last-render-ms]
         [(compute-label "Average" (.startsWith (name sort-key) "avg") sort-asc?) :avg-render-ms]
         [(compute-label "Worst" (.startsWith (name sort-key) "max") sort-asc?) :max-render-ms]
         [(compute-label "Best" (.startsWith (name sort-key) "min") sort-asc?) :min-render-ms]
         [(compute-label "Std. deviation" (.endsWith (name sort-key) "std-dev") sort-asc?) :render-std-dev]]))))

(defui Statistics
  Object
  (initLocalState [this]
    {:visible? false})
  (componentDidMount [this]
    (let [{:keys [toggle-shortcut]} (om/shared this)
          {:keys [visible?]} (om/get-state this)]
      (kbd/register-key-handler this
        {toggle-shortcut #(om/update-state! this update-in [:visible?] not)})))
  (componentWillUnmount [this]
    (kbd/dispose-key-handler this))
  (render [this]
    (let [stats (generate-stats (om/props this))
          {:keys [visible?]} (om/get-state this)]
      (dom/figure nil
        (dom/table #js {:className "instrumentation-table"
                        :style #js {:display (if visible? "table" "none")}}
          (dom/thead nil
            (dom/tr nil
              (dom/td nil)
              (dom/th #js {:colSpan 12
                           :className "number"} "Render | Mount"))
            (render-table-headers this))
          (dom/tbody nil
            (map-indexed #(stats-row (assoc %2 :react-key %1)) stats))
          (dom/tfoot nil
            (dom/tr nil
              (dom/td #js {:className "instrumentation-info" :colSpan "13"}
                #_(gstring/format "Component render stats, sorted by %s (%s). Clicks go through. %s to toggle, %s to clear."
                    sort-order
                    (format-shortcut sort-shortcut)
                    (format-shortcut toggle-shortcut)
                    (format-shortcut clear-shortcut))))))))))

(defonce reconciler
  (om/reconciler {:state (atom {:measurements nil
                                :sort-key :component-name
                                :sort-asc? true})
                  :shared {:toggle-shortcut #{"shift" "meta" "s"}}}))

(defn prepend-stats-node [classname]
  (let [node (gdom/htmlToDocumentFragment (gstr/format "<div class='%s'></div>" classname))
        body js/document.body]
    (.insertBefore body node (.-firstChild body))
    node))

(defn instrument
  ([] (instrument (constantly nil)))
  ([f]
   (let [class "plomber"
         node  (or (gdom/getElementByClass class)
                   (prepend-stats-node class))]
     (om/add-root! reconciler Statistics node)
     (fn [{:keys [props children class factory] :as m}]
       (f m)
       (wrap-lifecycle-methods (om/app-state reconciler) class)
       (apply factory props children)))))

;; TODO:
;; - key bindings
;; - 
