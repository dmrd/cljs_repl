(ns cljs-repl.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defonce db (r/atom {:history []}))

(defn process-inputs [inputs]
  (swap! db update :history conj inputs)  ; placeholder: append input directly
  )

(defn repl []
  (let [repl_value (r/atom "")]
    (fn []
      [:div
       [:input {:type :text :value @repl_value
                :on-change #(reset! repl_value (-> % .-target .-value))
                :on-key-press (fn [e]
                                (println "key press" (.-charCode e))
                                (if (= 13 (.-charCode e))
                                  (do
                                    (process-inputs @repl_value)
                                    (reset! repl_value "")
                                    )))
                }]])))

(defn history [] [:ul
                  (for [entry (rseq @(r/cursor db [:history]))]
                    ^{:key entry} [:li entry]
                    )
                  ])

(defn ui []
  [:div
   [repl]
   [history]])

(defn render []
  (rdom/render [ui] (js/document.getElementById "app"))
  )

(defn ^:dev/after-load reload! [] (render))
(defn ^:export main! [] (render))
