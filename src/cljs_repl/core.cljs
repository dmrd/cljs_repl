(ns cljs-repl.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [sci.core :as sci]
            ))

(defonce db (r/atom {:history []}))

(defn sci-execute-with-handler [inputs]
  (try (let [output (sci/eval-string inputs)]
         {:output output}
         )
       (catch :default e {:exception e}))
  )

(defn process-inputs [inputs]
  (if (not= 0 (count inputs))
    (let [output-map (sci-execute-with-handler inputs)
          combined (merge {:input inputs} output-map)
          ]
                                        ;(swap! db update :history conj combined)
      (swap! db update :history conj combined)
      )
    ))

(defn ui-repl []
  (let [repl_value (r/atom "")]
    (fn []
      [:div ">"
       [:input {:type :text :value @repl_value
                :on-change #(reset! repl_value (-> % .-target .-value))
                :on-key-press (fn [e]
                                (if (= 13 (.-charCode e))
                                  (do
                                    (process-inputs @repl_value)
                                    (reset! repl_value "")
                                    )))
                }]])))

(defn ui-history-entry [entry]
  (let [input (:input entry)
        output (pr-str (if (contains? entry :exception)
                         (:exception entry)
                         (:output entry)))
        formatted (clojure.string/join " => " [input output])
        ]
    [:li formatted]
    )
  )

(defn ui-history [] [:ul
                     (for [entry (rseq @(r/cursor db [:history]))]
                       ^{:key entry} [ui-history-entry entry]
                       )
                     ])

(defn ui []
  [:div
   [ui-repl]
   [ui-history]])

(defn render []
  (rdom/render [ui] (js/document.getElementById "app"))
  )

(defn ^:dev/after-load reload! [] (render))
(defn ^:export main! [] (render))
