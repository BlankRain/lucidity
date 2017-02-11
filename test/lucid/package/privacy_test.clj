(ns lucid.package.privacy-test
  (:use hara.test)
  (:require [lucid.package.privacy :refer :all]
            [lucid.package.user :as package]
            [hara.io
             [file :as fs]
             [encode :as encode]]
            [hara.security :as security]))

^{:refer lucid.package.privacy/load-public-keyring :added "1.2"}
(comment "loads a public keyring"

  (load-public-keyring package/GNUPG-PUBLIC))

^{:refer lucid.package.privacy/load-secret-keyring :added "1.2"}
(comment "loads a secret keyring"

  (load-secret-keyring package/GNUPG-SECRET))

^{:refer lucid.package.privacy/save-keyring :added "1.2"}
(comment "saves a keyring to file"

  (-> package/GNUPG-SECRET
      (load-secret-keyring)
      (save-keyring "hello.gpg")))

^{:refer lucid.package.privacy/all-public-keys :added "1.2"}
(comment "returns all public keys within a keyring"

  (-> package/GNUPG-PUBLIC
      (load-public-keyring)
      (all-public-keys)))

^{:refer lucid.package.privacy/fingerprint :added "1.2"}
(comment "returns the fingerprint of a public key"

  (-> package/GNUPG-PUBLIC
      (load-public-keyring)
      (all-public-keys)
      (first)
      (fingerprint))
  => "9B94FD0EA99482F6BC00777313319CB698B9A74D")

^{:refer lucid.package.privacy/get-public-key :added "1.2"}
(comment "returns public key given a partial fingerprint"

  (-> package/GNUPG-PUBLIC
      (load-public-keyring)
      (get-public-key "9B94FD0E")))

^{:refer lucid.package.privacy/all-secret-keys :added "1.2"}
(comment "returns all secret keys within a keyring"

  (-> package/GNUPG-SECRET
      (load-secret-keyring)
      (all-secret-keys)))

^{:refer lucid.package.privacy/get-secret-key :added "1.2"}
(comment "returns secret key given a fingerprint"

  (-> package/GNUPG-SECRET
      (load-secret-keyring)
      (get-secret-key "9B94FD0E")))

^{:refer lucid.package.privacy/get-keypair :added "1.2"}
(comment "returns public and private keys given a fingerprint"

  (-> package/GNUPG-SECRET
      (load-secret-keyring)
      (get-keypair "9B94FD0E"))
  ;;=> [#key.public[9B94FD0EA99482F6BC00777313319CB698B9A74D]
  ;;    #key.private[1383058868639737677]]
  )

^{:refer lucid.package.privacy/decrypt :added "1.2"}
(comment "returns the decrypted file given a keyring file"

  (decrypt package/LEIN-CREDENTIALS-GPG
           package/GNUPG-SECRET))

^{:refer lucid.package.privacy/crc-24 :added "1.2"}
(fact "returns the crc24 checksum "

  (crc-24 (byte-array [100 100 100 100 100 100]))
  => ["=6Fko" [-24 89 40] 15227176])

^{:refer lucid.package.privacy/generate-signature :added "1.2"}
(comment "generates a signature given bytes and a keyring"
  
  (generate-signature (fs/read-all-bytes "project.clj")
                      (load-secret-keyring lucid.package.user/GNUPG-SECRET)
                      "98B9A74D"))

^{:refer lucid.package.privacy/sign :added "1.2"}
(comment "generates a output gpg signature for an input file"

  (sign "project.clj"
        "project.clj.asc"
        lucid.package.user/GNUPG-SECRET
        "98B9A74D"))




(comment
  (require '[hara.io.encode :as encode])
  
  (-> (encode/from-base64 "iQEcBAABCAAGBQJXwO1zAAoJEBMxnLaYuadN0msH/2WuYDY193gbkaq1mExeigswIdf0eIi1jlthkfnjjRbNYHXujv3/o3DMoc7kKfdh29y+nalteHB668xPrbs8s2gZGzjMYbhQzI3UXBBltn+/bWv+j9wfs2eumkODxdUa4MHixDU5ZbKjdcnYxqHz5n+qc79tR/WYAmsiNIfbg9k8xIWZM06PnhbDhpnZAxXABrwQt75ygFtEaVKvOlRH2T9h6IuRjnvPNqdX65X9uleCNVHiSEEDWh7uZgc6SepU4iIDj3nolZRE4Cdd7HsI4SfRmqpvbeGovjl5olx1nCcQxK5D7Yy9IpZtvhBWdz0CQm+9U+9rT1+Um0UNCZUpCAY=")
      (crc-24))
  

  
  (verify signature file key)
  
  )
(comment
  (def signature (load-signature "project.clj.asc"))
  (def orig (fs/read-all-bytes "project.clj"))
  (def public-key
    (openpgp->key (encode/from-base64 "mQENBFh1kBcBCADMUkNW2qFgeRnornjhT3lt73wTGcO9rt+Ktr1tcopmOPTfNq3feZNFHRUsBt0Nnuj9+vSD2cwFRoZDNulhnBD8lAJYOD427uvV+KBDF/5pCQKh2SmDK8tJI/ncLIlX4SFa8F9f36FySglpkzA59IFtHdUBz9w+PJRqUQ5MVRzNHYBbv6aeIWwl46KrL3eibRgBDVuEOKAoesdb+xErs9cqg3KSVi01XBgr+XMSgOBz4J3fJ4HdibsJdz1+113aKT++4LUSuuyeVbw3K/ZgMkrsyeJw84sHhF2kDu61atSUsQEnJwBF2sPA9V/i28fftxodgg5qbEs8egdsw/wxGzsfABEBAAG0K0NocmlzIFpoZW5nIChjemhlbmcpIDxjemhlbmdAYXRsYXNzaWFuLmNvbT6JATkEEwEIACMFAlh1kBcCGwMHCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAKCRARY82BslwTrMvtB/4tLoRH91p09vM1sSQ77RC+XwQqhhvN1BAeqGxqZpgCO6Ld5KRh8f4mFY8nXjDCSyyydTBzIUp6aG0f+2EBhArtk/oU/pi8D4zHoeBkrl23/234s1kBI5F2g2kd6itwP8ekimaUyNFdPIN1dPwdxhuspOUtFNR+HsT3BT32v4Afd7sWVNTFrkapxTdxxZkVb+FS0wbuzFzB2gb8AvEGevzF/hQXOf3r8QzUQoEZ14pigNu/arlXzEuzXJXXT/AQnAzbVENoFrhojxqEU7RxH8J4nao8OfpYfL7w3T7PC3nFFSTYSwvYItGkl9DPPiZGWZTrb6VfpFGNLHwCoLOaf8ShsAIAAA==")))
  (security/verify orig signature public-key {:algorithm "SHA256withRSA"})
  
  (CMSSignedData. signature)

  
  (require '[lucid.mind :refer :all])
  (import org.bouncycastle.cms.CMSSignedData)
  (unit/import)

  (def pattern "25CD2515BBF92")
  
  (def rcoll (load-secret-keyring lucid.package.user/GNUPG-SECRET))

  (def kpair (get-keypair rcoll pattern))

  (def pub-key (first kpair))

  (def pri-key (second kpair))
  
  (def sig (generate-signature (fs/read-all-bytes "project.clj")
                               (load-secret-keyring lucid.package.user/GNUPG-SECRET)
                               pattern))

  )
