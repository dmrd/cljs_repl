(ns cljs-repl.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [sci.core :as sci]
            ))

(defonce db (r/atom {:history []
                     :env {}
                     :prompt-state ""}))

(defn sci-execute-with-handler [inputs env_atom]
  (try (let [output (sci/eval-string inputs {:env env_atom})]
         {:output output}
         )
       (catch :default e {:exception e}))
  )

(defn process-inputs [inputs env_atom]
  (if (not= 0 (count inputs))
    (let [output-map (sci-execute-with-handler inputs env_atom)
          combined (merge {:input inputs} output-map)
          ]
                                        ;(swap! db update :history conj combined)
      (swap! db update :history conj combined)
      )
    ))

(defn ui-repl [prompt_state_atom env_atom]
    (fn []
      [:div ">"
       [:input {:type :text :value @prompt_state_atom
                :on-change #(reset! prompt_state_atom (-> % .-target .-value))
                :on-key-press (fn [e]
                                (if (= 13 (.-charCode e))
                                  (do
                                    (process-inputs @prompt_state_atom env_atom)
                                    (reset! prompt_state_atom "")
                                    )))
                }]]))

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
   [ui-repl (r/cursor db [:prompt_state]) (r/cursor db [:env])]
   [ui-history]])

(defn render []
  (rdom/render [ui] (js/document.getElementById "app"))
  )

(defn ^:dev/after-load reload! [] (render))
(defn ^:export main! [] (render))
