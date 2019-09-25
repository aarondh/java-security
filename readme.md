## Daisley-Harrison Cyber Security Samples

The Daisley-Harrison samples are designed to demonstrate cyber security policies by providing a reference architecture that IT can emulate and/or reuse to help increase the adoption of secure methods and best practices in web application construction.

This set of samples are currently a POC (Proof of Concept) and should not yet be used to base application development on.

Watch this space as in the coming months we will continue to improve the samples and associated security libraries.

#Notes:

### generate RSA public/private key pair

keytool -genkey -alias vault -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore dirkeystore.pkcs12 

### generating a jwe key
keytool -genseckey -alias jwe-bearer -keyalg AES -keysize 256 -storetype Jks -keystore cypherstore.jks
keytool -genseckey -alias jwe-state -keyalg AES -keysize 256 -storetype Jks -keystore cypherstore.jks

keytool.exe -genseckey -alias bearer-ChaCha -keyalg ChaCha20 -keysize 128 -storetype PKCS12 -keystore testkeystore.pkcs12 -providerclass org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath "C:\Users\Adaisle1\.m2\repository\org\bouncycastle\bcprov-jdk15on\1.62\bcprov-jdk15on-1.62.jar

