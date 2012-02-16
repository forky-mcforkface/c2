(ns c2.core-test
  (:use-macros [helpers :only [p profile]])
  (:use [c2.core :only [unify!]]
        [c2.dom :only [attr children]]))

(defn *print-fn* [x]
  (.log js/console x))

(def xhtml "http://www.w3.org/1999/xhtml")

(def container (.createElementNS js/document xhtml "div"))
;;Appending to html instead of body here because of PhantomJS page.injectJs() wonky behavior
(.appendChild (.querySelector js/document "html") container)

(defn clear! [] (set! (.-innerHTML container) ""))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Attribute reading & writing
(assert (= nil (attr container :x)))
(attr container :x 1)
(assert (= "1" (attr container :x)))
(attr container {:y 2 :z 3})
(assert (= {:x "1" :y "2" :z "3"} (attr container)))



(print "\n\nSingle node enter/update/exit\n=============================")
(let [n 100
      mapping (fn [d idx] [:span {:x d} (str d)])]
  
  (profile (str "ENTER single tag with " n " data")
           (unify! container (range n) mapping))
  (let [children (children container)
        fel       (first children)]
    (assert (= n (count children)))
    (assert (= "span" (.toLowerCase (.-nodeName fel))))
    (assert (= "0" (:x (attr fel)))))


  (profile (str "UPDATE single tag, reversing order")
           (unify! container (reverse (range n)) mapping))
  (let [children (children container)
        fel       (first children)]
    (assert (= n (count children)))
    (assert (= "span" (.toLowerCase (.-nodeName fel))))
    (assert (= (str (dec n)) (:x (attr fel)))))

  
  (profile (str "UPDATE single tag with new datum")
           (unify! container (range (inc n)) mapping))
  (assert (= (inc n) (count (children container))))


  (profile (str "REMOVE " (/ n 2) " single tags")
           (unify! container (range (/ n 2)) mapping))
  (assert (= (/ n 2)  (count (children container)))))



(clear!)

(print "\n\nMore complex dataset\n====================")
(let [n 100
      data (map #(hash-map :id % :val (str (rand)))
                (range n))
      new-data (map #(assoc % :val (str (rand))) data)
      mapping (fn [d idx] [:div {:val (:val d)}
                          [:span (str (:id d))]])]
  (profile "ENTER node hiearchy"
           (unify! container data mapping
                   :key-fn :id))
  (assert (= 100 (count (children container))))

  (profile "UPDATE/EXIT node hiearchy"
           (unify! container (take 10 new-data) mapping
                   :key-fn :id))
  
  (assert (= 10 (count (children container))))
  (assert (= (:val (first new-data))
             (:val (attr (first (children container)))))))



(print "\n\nHurray, no errors!")