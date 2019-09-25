package org.daisleyharrison.security.services.vault.models;

import java.security.spec.AlgorithmParameterSpec;
import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.daisleyharrison.security.common.models.key.KeySpecification;
import org.daisleyharrison.security.common.models.cypher.CypherSpecification;
import org.daisleyharrison.security.common.models.key.KeyReference;

public class CypherMetaData implements KeySpecification {
    private static final String VAULT_KEY_PATH_PREFIX = ".vault/";
    private static final String PRIVATE_KEY_PATH_SUFFIX = "/private";
    private static final String PUBLIC_KEY_PATH_SUFFIX = "/public";
    private static final String SECRET_KEY_PATH_SUFFIX = "/secret";
    public static final String RSA_ALGORITHM_PREFIX = "RSA";
    public static final String RS_ALGORITHM_PREFIX = "RS";
    public static final String AES_ALGORITHM_PREFIX = "AES";
    private static final String DEFAULT_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_KEY_GENERATOR = "AES";
    private static final int DEFAULT_KEY_SIZE = 128;

    @JsonProperty("keyPath")
    private String keyPath;
    @JsonProperty("password")
    private char[] password;
    @JsonProperty("algorithm")
    private String algorithm;
    @JsonProperty("keyGenerator")
    private String keyGenerator;
    @JsonProperty("keySize")
    private int keySize;
    @JsonProperty("ttl")
    private String ttl;
    @JsonProperty("issuer")
    private String issuer;
    @JsonProperty("subject")
    private String subject;

    public CypherMetaData() {
        this.algorithm = DEFAULT_ALGORITHM;
        this.keyGenerator = DEFAULT_KEY_GENERATOR;
        this.keySize = DEFAULT_KEY_SIZE;
    }

    public CypherMetaData(String keyPath) {
        this.algorithm = DEFAULT_ALGORITHM;
        this.keyPath = keyPath;
    }

    public CypherMetaData(String keyPath, String algorithm) {
        this.algorithm = algorithm;
        this.keyPath = keyPath;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public static String toPrivateKeyPath(String keyPath) {
        return keyPath + PRIVATE_KEY_PATH_SUFFIX;
    }

    public static String toPublicKeyPath(String keyPath) {
        return keyPath + PUBLIC_KEY_PATH_SUFFIX;
    }

    public static String toSecretKeyPath(String keyPath) {
        return keyPath + SECRET_KEY_PATH_SUFFIX;
    }

    public static boolean isVaultKeyPath(String keyPath) {
        return keyPath == null ? false : keyPath.startsWith(VAULT_KEY_PATH_PREFIX);
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public static boolean isPublicKeyPath(String keyPath) {
        return keyPath == null ? false : keyPath.endsWith(PUBLIC_KEY_PATH_SUFFIX);
    }

    public static boolean isPrivateKeyPath(String keyPath) {
        return keyPath == null ? false : keyPath.endsWith(PRIVATE_KEY_PATH_SUFFIX);
    }

    public static boolean isSecretKeyPath(String keyPath) {
        return keyPath == null ? false : keyPath.endsWith(SECRET_KEY_PATH_SUFFIX);
    }

    private static String removeSuffix(String keyPath, String suffix) {
        if (keyPath != null && keyPath.endsWith(suffix)) {
            return keyPath.substring(0, keyPath.length() - suffix.length());
        } else {
            return keyPath;
        }
    }

    public static String toAliasFromKeyPath(String keyPath) {
        String alias = null;
        if (keyPath != null) {
            if (keyPath.startsWith(VAULT_KEY_PATH_PREFIX)) {
                alias = keyPath.substring(VAULT_KEY_PATH_PREFIX.length());
            }
            alias = removeSuffix(alias, PUBLIC_KEY_PATH_SUFFIX);
            alias = removeSuffix(alias, PRIVATE_KEY_PATH_SUFFIX);
            alias = removeSuffix(alias, SECRET_KEY_PATH_SUFFIX);
        }
        return alias;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public char[] getPassword() {
        return password;
    }

    /**
     * @return String return the algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @return int return the keySize
     */
    public int getKeySize() {
        return keySize;
    }

    /**
     * @param keySize the keySize to set
     */
    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    /**
     * @return String return the ttl
     */
    public String getTtl() {
        return ttl;
    }

    /**
     * @param ttl the ttl to set
     */
    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    @Override
    public Duration getTTLDuration() {
        if (this.ttl == null) {
            return Duration.ZERO;
        }
        return Duration.parse(this.ttl);
    }

    /**
     * @return String return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @param issuer the issuer to set
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * @return String return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public KeyReference toKeySpec() {
        return toKeyRef(null);
    }

    public KeyReference toKeyRef(char[] password) {
        if (this.password != null) {
            password = this.password;
        }
        return new KeyRefImpl(getKeyPath(), getKeySize(), password);
    }

    public CypherSpecification toCypherSpec(AlgorithmParameterSpec parameters) {
        return new CypherSpecImpl(getAlgorithm(), parameters);
    }

    public CypherSpecification toCypherSpec() {
        return new CypherSpecImpl(getAlgorithm());
    }

}