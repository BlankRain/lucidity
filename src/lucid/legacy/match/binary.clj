;; WARNING: this namespace is experimental

(ns ^{:skip-wiki true}
  lucid.legacy.match.binary
  (:refer-clojure :exclude [compile])
  (:use [lucid.legacy.match :as m]))

(derive :lucid.legacy.match/binary :lucid.legacy.match/vector)

(defmethod check-size? :lucid.legacy.match/binary
  [_] false)

(defmethod test-inline :lucid.legacy.match/binary
  [_ ocr] `(instance? Long ~ocr))

(defmethod nth-inline :lucid.legacy.match/binary
  [_ ocr i] `(bit-shift-right (bit-and ~ocr (bit-shift-left 1 ~i)) ~i))

(comment
  (let [x 5]
    (match [x]
      [([_ _ 1 1] :lucid.legacy.match/binary)] :a0
      [([1 0 1 _] :lucid.legacy.match/binary)] :a1
      :else :a2))
  
  (let [x 5]
    (dotimes [_ 10]
      (time
       (dotimes [_ 1e7]
         (match [x]
           [([_ _ 1 1] :lucid.legacy.match/binary)] :a0
           [([1 0 1 _] :lucid.legacy.match/binary)] :a1
           :else :a2)))))
  )

(comment
  (match [dgram]
    [([(ip-version 4)
       ((hlen 4) :when [#(>= % 5) #(<= (* 4 %) drgramsize)])
       (srvc-type 8)
       (totlen 16)
       (id 16)
       (flgs 3)
       (fragoff 13)
       (ttl 8)
       (proto 8)
       (hdrchksum 16)
       (srcip 32)
       (destip 32)
       & restdgram] :lucid.legacy.match/binary)])
  )

;; Erlang
;;
;; -define (IP_VERSION, 4).
;; -define (IP_MIN_HDR_LEN, 5).
;; DgramSize = byte_size (Dgram),
;; case Dgram of
;; <<?IP_VERSION:4, HLen:4, SrvcType:8, TotLen:16,
;; ID:16, Flgs:3, FragOff:13,
;; TTL:8, Proto:8, HdrChkSum:16,
;; SrcIP:32,
;; DestIP:32, RestDgram/binary>> when HLen>=5, 4*HLen=<DgramSize ->
;; OptsLen = 4* (HLen - ?IP_MIN_HDR_LEN),
;; <<Opts:OptsLen/binary,Data/binary>> = RestDgram,
;; ...
;; end.
