(ns lucid.package.privacy
  (:require [hara.io.file :as fs]
            [hara.io.encode :as encode]
            [hara.security :as security]
            [clojure.string :as string])
  (:import (java.security Security)
           (org.bouncycastle.openpgp PGPObjectFactory
                                     PGPPublicKey
                                     PGPPrivateKey
                                     PGPSecretKey
                                     PGPSignature
                                     PGPSignatureGenerator
                                     PGPUtil)
           (org.bouncycastle.bcpg CRC24)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.openpgp.jcajce JcaPGPObjectFactory)
           (org.bouncycastle.openpgp.operator.jcajce JcePBESecretKeyDecryptorBuilder)
           (org.bouncycastle.openpgp.operator.bc BcKeyFingerprintCalculator
                                                 BcPublicKeyDataDecryptorFactory
                                                 BcPGPContentSignerBuilder)
           (org.bouncycastle.openpgp.bc BcPGPPublicKeyRingCollection
                                        BcPGPSecretKeyRingCollection)))

(defonce +bouncy-castle+ 
  (Security/addProvider (BouncyCastleProvider.)))

(defn load-public-keyring
  "loads a public keyring
 
   (load-public-keyring package/GNUPG-PUBLIC)"
  {:added "1.2"}
  [input]
  (-> (fs/input-stream input)
      (BcPGPPublicKeyRingCollection.)))

(defn load-secret-keyring
  "loads a secret keyring
 
   (load-secret-keyring package/GNUPG-SECRET)"
  {:added "1.2"}
  [input]
  (-> (fs/input-stream input)
      (BcPGPSecretKeyRingCollection.)))

(defn save-keyring
  "saves a keyring to file
 
   (-> package/GNUPG-SECRET
      (load-secret-keyring)
       (save-keyring \"hello.gpg\"))"
  {:added "1.2"}
  [keyring path]
  (->> (fs/output-stream path)
       (.encode keyring)))

(defn all-public-keys
  "returns all public keys within a keyring
 
   (-> package/GNUPG-PUBLIC
      (load-public-keyring)
       (all-public-keys))"
  {:added "1.2"}
  [rcoll]
  (->> (.getKeyRings rcoll)
       (iterator-seq)
       (map #(->> %
                  (.getPublicKeys)
                  (iterator-seq)))
       (apply concat)))

(defn fingerprint
  "returns the fingerprint of a public key
 
   (-> package/GNUPG-PUBLIC
       (load-public-keyring)
       (all-public-keys)
       (first)
       (fingerprint))
   => \"9B94FD0EA99482F6BC00777313319CB698B9A74D\""
  {:added "1.2"}
  [pub]
  (-> pub
      (.getFingerprint)
      (hara.io.encode/to-hex)
      (.toUpperCase)))

(defmethod print-method PGPPublicKey
  [v ^java.io.Writer w]
  (.write w (str "#gpg.public[" (fingerprint v) "]")))

(defn get-public-key
  "returns public key given a partial fingerprint
 
   (-> package/GNUPG-PUBLIC
      (load-public-keyring)
       (get-public-key \"9B94FD0E\"))"
  {:added "1.2"}
  [rcoll sig]
  (->> (all-public-keys rcoll)
       (filter #(-> %
                    (fingerprint)
                    (.contains (.toUpperCase sig))))
       (first)))

(defn all-secret-keys
  "returns all secret keys within a keyring
 
   (-> package/GNUPG-SECRET
      (load-secret-keyring)
       (all-secret-keys))"
  {:added "1.2"}
  [rcoll]
  (->> (.getKeyRings rcoll)
       (iterator-seq)
       (map #(->> %
                  (.getSecretKeys)
                  (iterator-seq)))
       (apply concat)))

(defmethod print-method PGPSecretKey
  [v ^java.io.Writer w]
  (.write w (str "#gpg.secret[" (fingerprint (.getPublicKey v)) "]")))

(defn get-secret-key
  "returns secret key given a fingerprint
 
   (-> package/GNUPG-SECRET
      (load-secret-keyring)
       (get-secret-key \"9B94FD0E\"))"
  {:added "1.2"}
  [rcoll sig]
  (cond (string? sig)
        (->> (all-secret-keys rcoll)
             (filter #(-> %
                          (.getPublicKey)
                          (fingerprint)
                          (.contains (.toUpperCase sig))))
             (first))
        
        :else
        (->> (.getKeyRings rcoll)
             (iterator-seq)
             (keep #(.getSecretKey % sig))
             (first))))

(defmethod print-method PGPPrivateKey
  [v ^java.io.Writer w]
  (.write w (str "#gpg.private[" (.getKeyID v) "]")))

(defn get-keypair
  "returns public and private keys given a fingerprint
 
   (-> package/GNUPG-SECRET
       (load-secret-keyring)
       (get-keypair \"9B94FD0E\"))
   ;;=> [#key.public[9B94FD0EA99482F6BC00777313319CB698B9A74D]
   ;;    #key.private[1383058868639737677]]
   "
  {:added "1.2"}
  [rcoll sig]
  (let [decryptor (-> (JcePBESecretKeyDecryptorBuilder.)
                      (.setProvider "BC")
                      (.build (char-array "")))
        secret-key (get-secret-key rcoll sig)]
    (if secret-key
      [(.getPublicKey secret-key)
       (.extractPrivateKey secret-key decryptor)])))

(defn decrypt
  "returns the decrypted file given a keyring file
 
   (decrypt package/LEIN-CREDENTIALS-GPG
           package/GNUPG-SECRET)"
  {:added "1.2"}
  [encrypted-file keyring-file]
  (let [obj-factory  (-> (fs/input-stream encrypted-file)
                         (PGPUtil/getDecoderStream)
                         (PGPObjectFactory. (BcKeyFingerprintCalculator.)))
        rcoll         (load-secret-keyring keyring-file)
        enc-data     (-> (.nextObject obj-factory)
                         (.getEncryptedDataObjects)
                         (iterator-seq)
                         (first))
        key-id       (.getKeyID enc-data)
        [_ prv-key]  (get-keypair rcoll key-id)
        clear-stream (-> (.getDataStream enc-data
                                         (BcPublicKeyDataDecryptorFactory. prv-key))
                         (JcaPGPObjectFactory.)
                         (.nextObject)
                         (.getDataStream)
                         (JcaPGPObjectFactory.)
                         (.nextObject)
                         (.getDataStream))]
    (slurp clear-stream)))

(defn generate-signature
  "generates a signature given bytes and a keyring
   
   (generate-signature (fs/read-all-bytes \"project.clj\")
                       (load-secret-keyring lucid.package.user/GNUPG-SECRET)
                       \"98B9A74D\")"
  {:added "1.2"}
  [bytes rcoll sig]
  (let [[pub-key prv-key]  (get-keypair rcoll sig)
        sig-gen  (-> (BcPGPContentSignerBuilder.
                      (.getAlgorithm pub-key)
                      PGPUtil/SHA256)
                     (PGPSignatureGenerator.))
        sig-gen  (doto sig-gen
                   (.init PGPSignature/DEFAULT_CERTIFICATION prv-key)
                   (.update bytes))]
    (.generate sig-gen)))

(defmethod print-method PGPSignature
  [v ^java.io.Writer w]
  (.write w (str "#gpg.signature [\"" (encode/to-base64 (.getEncoded v)) "\"]")))

(defn crc-24
  "returns the crc24 checksum 
 
   (crc-24 (byte-array [100 100 100 100 100 100]))
   => [\"=6Fko\" [-24 89 40] 15227176]"
  {:added "1.2"}
  [input]
  (let [crc (org.bouncycastle.bcpg.CRC24.)
        _   (doseq [i (seq input)]
              (.update crc i))
        val (.getValue crc)
        bytes (-> (biginteger val)
                  (.toByteArray)
                  seq)
        bytes (case (count bytes)
                4 (rest bytes)
                3 bytes
                (nth (iterate #(cons 0 %)
                              bytes)
                     (- 3 (count bytes))))]
    [(->> (byte-array bytes)        
          (encode/to-base64)
          (str "="))
     bytes
     val]))

(defn sign
  "generates a output gpg signature for an input file
 
   (sign \"project.clj\"
        \"project.clj.asc\"
         lucid.package.user/GNUPG-SECRET
         \"98B9A74D\")"
  {:added "1.2"}
  [input output keyring-file seed]
  (let [bytes (fs/read-all-bytes input)
        rcoll (load-secret-keyring keyring-file)
        signature  (-> (generate-signature bytes rcoll seed)
                       (.getEncoded))]
    (->> (concat ["-----BEGIN PGP SIGNATURE-----"
                  "Version: GnuPG v2"
                  ""]
                 (->> signature
                      (encode/to-base64)
                      (partition-all 64)
                      (map #(apply str %)))
                 [(first (crc-24 signature))
                  "-----END PGP SIGNATURE-----"])
         (string/join "\n")
         (spit output))))


(defn load-signature [signature-file]
  (->> (slurp signature-file)
       (string/split-lines)
       (reverse)
       (drop-while (fn [input]
                     (not (and (.startsWith input "=")
                               (= 5 (count input))))))
       (rest)
       (take 6)
       (reverse)
       (string/join "")
       (encode/from-base64)))

;(load-signature "project.clj.asc")

(defn verify
  [input signature-file public-key]
  (let [bytes (fs/read-all-bytes input)
        sig (load-signature signature-file)
        ])
  
  
  )

(comment
  
  (require '[lucid.package.user :as u])
  
  (def rcoll (load-secret-keyring u/GNUPG-SECRET))
  
  (def pair (get-keypair rcoll "98B9A74D"))

  (def public-key (first pair))
  
  (.? public-key)

  

  
  

  )  
