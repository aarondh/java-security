package org.daisleyharrison.security.services.tokenizer;

import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.utilities.StageImpl;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.DatastoreCursor;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.models.key.KeyProvider.KeyVersion;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.authorization.TokenMetaData;
import org.daisleyharrison.security.data.models.TokenContainer;
import org.daisleyharrison.security.jose4j.Jose4JExtensions;
import org.daisleyharrison.security.jose4j.jwe.ExtendedContentEncryptionAlgorithmIdentifiers;
import org.daisleyharrison.security.services.tokenizer.models.TokenGenerator;
import org.daisleyharrison.security.services.tokenizer.models.WebTokenDefinition;
import org.daisleyharrison.security.services.tokenizer.models.KeyRefImpl;
import org.daisleyharrison.security.services.tokenizer.models.RandomTokenGeneratorImpl;
import org.daisleyharrison.security.common.utilities.AuthClaimsImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.management.ServiceNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.daisleyharrison.security.common.exceptions.DatastoreException;
import org.daisleyharrison.security.common.exceptions.InvalidTokenException;
import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.exceptions.NonceExpiredException;
import org.daisleyharrison.security.common.exceptions.NonceReplayException;
import org.daisleyharrison.security.common.exceptions.TokenExpiredException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

public final class TokenizerService implements TokenizerServiceProvider {

    public enum State {
        CREATED("Created"), INITIALIZING("Initializing"), INITIALIZED("Initialized"), CLOSED("Closed"),
        COMPROMIZED("Compromized");

        private String label;

        private State(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private static final LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    private static Logger LOGGER = LoggerFactory.getLogger(TokenizerService.class);
    public static final ObjectMapper s_objectMapper = new ObjectMapper();
    public static final String DEFAULT_TOKEN_TYPE = "default";
    public static final String DEFAULT_TOKEN_NAMESPACE = "";
    public static final int DEFAULT_TOKEN_LENGTH = 16;
    public static final String NONCE_HASH_ALGORITHM = "SHA-256";
    public static final String NONCE_TOKEN_TYPE = "nonce";
    public static final String NONCE_TOKEN_NAMESPACE = "";
    public static final int NONCE_TOKEN_LENGTH = 16;
    private static final String CHACHA20_NONCE_TOKEN_TYPE = ExtendedContentEncryptionAlgorithmIdentifiers.AEAD_CHACHA20_POLY1305;
    private static final String CHACHA20_NONCE_TOKEN_NAMESPACE = "";
    private static final int CHACHA20_NONCE_TOKEN_LENGTH = 12;
    private State state;
    private Map<String, TokenGenerator> tokenGenerators = new HashMap<>();
    private DatastoreCollection<TokenContainer> tokenCollection;

    private static final String TOKEN_TYPES_PROPERTY = "tokenizer.token-types";
    private static final String TOKEN_DATASTORE_ACCESS_PROPERTY = "tokenizer.datastore.access";

    private Map<String, WebTokenDefinition> webTokenDefinitionsByType;

    private static TokenizerService _instance;

    public static TokenizerService getInstance() {
        if (_instance == null) {
            _instance = new TokenizerService();
        }
        return _instance;
    }

    protected TokenizerService() {
        Jose4JExtensions.extend();
        this.state = State.CREATED;
        this.webTokenDefinitionsByType = new HashMap<>();
        this.tokenGenerators.put(DEFAULT_TOKEN_TYPE,
                new RandomTokenGeneratorImpl(DEFAULT_TOKEN_NAMESPACE, DEFAULT_TOKEN_LENGTH));

        this.tokenGenerators.put(NONCE_TOKEN_TYPE,
                new RandomTokenGeneratorImpl(NONCE_TOKEN_NAMESPACE, NONCE_TOKEN_LENGTH));

        this.tokenGenerators.put(CHACHA20_NONCE_TOKEN_TYPE,
                new RandomTokenGeneratorImpl(CHACHA20_NONCE_TOKEN_NAMESPACE, CHACHA20_NONCE_TOKEN_LENGTH));

        Jose4JExtensions.extend(); // adds support for chacha20 to he jose4j library
    }

    private WebTokenDefinition getWebTokenDefinition(String webTokenType) throws TokenizerException {
        WebTokenDefinition webTokenDef = webTokenDefinitionsByType.get(webTokenType);
        if (webTokenDef == null) {
            throw new TokenizerException("invalid web token type");
        }
        return webTokenDef;
    }

    @Override
    public TokenMetaData getTokenMetaData(String webTokenType) {
        return webTokenDefinitionsByType.get(webTokenType);
    }

    @Override
    public boolean isInitialized() {
        return this.state != State.CREATED && this.state != State.CLOSED && this.state != State.INITIALIZING;
    }

    @Override
    public boolean isReady() {
        return this.state == State.INITIALIZED;
    }

    private void assertInitializing() {
        if (this.state != State.INITIALIZING) {
            throw new IllegalStateException(
                    "tokenizer-service is in state " + state.toString() + " and is not initializing.");
        }
    }

    private void assertReady() {
        if (!isReady()) {
            throw new IllegalStateException("tokenizer-service is in state " + state.toString() + " and is not ready.");
        }
    }

    private void assertDatastoreReady() {
        assertReady();
        if (this.tokenCollection == null) {
            throw new IllegalStateException("tokenizer-service does not have access to the datastore-service.");
        }
    }

    @Override
    public Stage beginInitialize() {
        if (this.state != State.CREATED && this.state != State.CLOSED) {
            throw new IllegalStateException(
                    "tokenizer-service is in state " + state.toString() + " and cannot be initialized.");
        }
        this.state = State.INITIALIZING;
        return new StageImpl(() -> {
            if (this.state != State.INITIALIZING) {
                throw new IllegalStateException(
                        "tokenizer-service is in state " + state.toString() + " and was not initialing.");
            }
            this.state = State.INITIALIZED;
        });
    }

    public void configure() {
        assertInitializing();
        try {

            ConfigurationServiceProvider config = _serviceProvider.provideService(ConfigurationServiceProvider.class);
            Set<String> jwtTokenTypes = config.getNames(TOKEN_TYPES_PROPERTY);
            for (String tokenType : jwtTokenTypes) {
                WebTokenDefinition jwtDef = new WebTokenDefinition(config, TOKEN_TYPES_PROPERTY, tokenType);
                webTokenDefinitionsByType.put(tokenType, jwtDef);
            }
            boolean datastoreAccess = config.getBooleanValue(TOKEN_DATASTORE_ACCESS_PROPERTY, true);
            if (datastoreAccess) {
                try {
                    DatastoreServiceProvider datastore = _serviceProvider
                            .provideService(DatastoreServiceProvider.class);
                    this.tokenCollection = datastore.openCollection(TokenContainer.class, "token");
                } catch (ServiceNotFoundException | DatastoreException exception) {
                    LOGGER.warn(
                            "tokenizer-service does not have access to the datastore-service, nonce and opaque token functionality not available");
                }
            } else {
                LOGGER.warn(
                        "tokenizer-service was configured to not access the datastore-service, nonce and opaque token functionality not available");
            }
        } catch (ServiceNotFoundException exception) {
            this.state = State.COMPROMIZED;
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public void close() {
        this.state = State.CLOSED;
    }

    private TokenGenerator getTokenGenerator(String tokenType) {
        TokenGenerator tokenGenerator = this.tokenGenerators.get(tokenType);
        if (tokenGenerator == null) {
            if (tokenType == DEFAULT_TOKEN_TYPE) {
                throw new IllegalStateException("No token generators available");
            }
            tokenGenerator = getTokenGenerator(DEFAULT_TOKEN_TYPE);
        }
        return tokenGenerator;
    }

    @Override
    public String produceOpaqueToken(Object payload, Date expires, String tokenType) throws TokenizerException {
        if (payload == null) {
            throw new IllegalStateException("payload cannot be null");
        }
        try {
            String payloadString;
            if (payload instanceof String) {
                payloadString = (String) payload;
            } else {
                payloadString = s_objectMapper.writeValueAsString(payload);
            }
            return produceOpaqueStringToken(payloadString, expires, tokenType);
        } catch (JsonProcessingException exception) {
            throw new TokenizerException(exception);
        }
    }

    @Override
    public String produceOpaqueToken(Object payload, Duration expires, String tokenType) throws TokenizerException {
        Date expiryDate = Date.from(new Date().toInstant().plus(expires));
        return produceOpaqueToken(payload, expiryDate, tokenType);
    }

    @Override
    public String produceOpaqueToken(Object payload, Date expires) throws TokenizerException {
        return this.produceOpaqueToken(payload, expires, DEFAULT_TOKEN_TYPE);
    }

    @Override
    public String produceOpaqueToken(Object payload, Duration expires) throws TokenizerException {
        Date expiryDate = Date.from(new Date().toInstant().plus(expires));
        return produceOpaqueToken(payload, expiryDate);
    }

    @Override
    public String produceOpaqueStringToken(String payload, Date expires, String tokenType) {
        if (payload == null) {
            throw new IllegalStateException("payload cannot be null");
        }
        if (expires == null) {
            throw new IllegalStateException("expires cannot be null");
        }

        assertDatastoreReady();

        String token = getTokenGenerator(tokenType).produceToken();
        TokenContainer container = new TokenContainer(token, expires, Integer.MAX_VALUE, payload);

        this.tokenCollection.insert(container);

        return token;
    }

    @Override
    public String produceOpaqueStringToken(String payload, Duration expires, String tokenType) {
        Date expiryDate = Date.from(new Date().toInstant().plus(expires));
        return produceOpaqueStringToken(payload, expiryDate, tokenType);
    }

    @Override
    public String produceOpaqueStringToken(String payload, Date expires) {
        return produceOpaqueStringToken(payload, expires, DEFAULT_TOKEN_TYPE);
    }

    @Override
    public String produceOpaqueStringToken(String payload, Duration expires) {
        Date expiryDate = Date.from(new Date().toInstant().plus(expires));
        return produceOpaqueStringToken(payload, expiryDate, DEFAULT_TOKEN_TYPE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T consumeOpaqueToken(String token, Class<T> type) throws InvalidTokenException {
        assertDatastoreReady();

        TokenContainer container = this.tokenCollection.findById(token);
        if (container == null) {
            throw new InvalidTokenException();
        } else {
            Date expires = container.getExpires();
            int uses = container.getUses();
            if (expires == null || expires.compareTo(new Date()) < 0) {
                this.tokenCollection.remove(container);
                throw new InvalidTokenException();
            }
            if (uses <= 1) {
                this.tokenCollection.remove(container);
                throw new InvalidTokenException();
            } else {
                container.setUses(uses - 1);
                this.tokenCollection.save(container);
            }
            if (type == String.class) {
                return (T) container.getPayload();
            } else {
                try {
                    return s_objectMapper.readValue(container.getPayload(), type);
                } catch (IOException exception) {
                    throw new InvalidTokenException(exception);
                }
            }
        }
    }

    @Override
    public void expire(String token) throws InvalidTokenException {
        assertDatastoreReady();
        TokenContainer container = this.tokenCollection.findById(token);
        if (container != null) {
            this.tokenCollection.remove(container);
        }
    }

    private static final String NONCE_NAMESPACE_SUFFIX = "/nonce/";
    private static final String NONCE_ID_NAMESPACE_SUFFIX = "/id/";

    /**
     * Check to make sure this nonce is used only once
     */
    @Override
    public void consumeNonce(String namespaceName, String nonce, Duration expires) throws TokenizerException {
        assertDatastoreReady();

        String id = namespaceName + NONCE_NAMESPACE_SUFFIX + nonce;
        TokenContainer container = this.tokenCollection.findById(id);
        if (container == null) {
            Date expiryDate = Date.from(new Date().toInstant().plus(expires));
            container = new TokenContainer(id, expiryDate, 0, "");
            this.tokenCollection.insert(container); // invalidate (uses==0)
        } else if (container.getUses() == 1 && !container.hasExpired()) {
            container.setUses(0);
            this.tokenCollection.save(container);
        } else {
            throw new NonceReplayException(String.format("Replay detected for nonce %s/%s", namespaceName, nonce));
        }
    }

    @Override
    public String produceNonce(String namespaceName, String id, Duration expires) throws TokenizerException {
        assertDatastoreReady();

        Date expiryDate = Date.from(new Date().toInstant().plus(expires));
        String nonce = getTokenGenerator(NONCE_TOKEN_TYPE).produceToken();
        String key = namespaceName + NONCE_ID_NAMESPACE_SUFFIX + id;
        TokenContainer container = this.tokenCollection.findById(key);
        if (container == null) {
            container = new TokenContainer(key, expiryDate, 1, nonce);
            this.tokenCollection.insert(container);
        } else {
            container.setExpires(expiryDate);
            container.setUses(1);
            container.setPayload(nonce);
            this.tokenCollection.save(container);
        }
        return nonce;
    }

    private static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private char[] byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return hexDigits;
    }

    private String bytesToHex(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }

    @Override
    public boolean consumeNonce(String namespaceName, String id, String client_nonce, String payload, String hash)
            throws TokenizerException {
        assertDatastoreReady();
        String key = namespaceName + NONCE_ID_NAMESPACE_SUFFIX + id;
        TokenContainer container = this.tokenCollection.findById(key);
        if (container == null || container.hasExpired() || container.getUses() <= 0) {
            throw new NonceExpiredException("Expired nonce");
        }

        // Consume the nonce and keep it for at least double it's expiry date
        String nonce = container.getPayload(); // retrieve the server generated nonce from the id
        long diffInMillies = container.getExpires().getTime() - new Date().getTime();
        consumeNonce(namespaceName, nonce, Duration.ofMillis(diffInMillies * 2));

        try {
            StringBuffer data = new StringBuffer();
            data.append(nonce);
            data.append(client_nonce);
            data.append(payload);

            final MessageDigest digest = MessageDigest.getInstance(NONCE_HASH_ALGORITHM);
            final byte[] hashBytes = hexToBytes(hash);

            final byte[] checkHashBytes = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));

            return MessageDigest.isEqual(hashBytes, checkHashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new TokenizerException("Invalid nonce", exception);
        }
    }

    @Override
    public String produceNonceHash(String nonce, String client_nonce, String payload) throws TokenizerException {
        assertReady();

        try {
            StringBuffer data = new StringBuffer();
            data.append(nonce);
            data.append(client_nonce);
            data.append(payload);

            final MessageDigest digest = MessageDigest.getInstance(NONCE_HASH_ALGORITHM);
            final byte[] hashBytes = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new TokenizerException("Invalid nonce", exception);
        }
    }

    private boolean isNonceRequired(WebTokenDefinition webTokenDef) {
        String alg = webTokenDef.getContentEncryptionAlg();

        if (alg.equals(ExtendedContentEncryptionAlgorithmIdentifiers.AEAD_CHACHA20_POLY1305)) {
            return true;
        }

        if (alg.equals(ExtendedContentEncryptionAlgorithmIdentifiers.AEAD_XCHACHA20_POLY1305)) {
            return true;
        }

        if (alg.equals(ContentEncryptionAlgorithmIdentifiers.AES_128_GCM)) {
            return true;
        }

        if (alg.equals(ContentEncryptionAlgorithmIdentifiers.AES_192_GCM)) {
            return true;
        }

        if (alg.equals(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM)) {
            return true;
        }

        return false;
    }

    private String produceOpaque(WebTokenDefinition webTokenDef, JwtClaims claims) throws Exception {
        return produceOpaqueStringToken(claims.toJson(), Duration.ofMinutes(webTokenDef.getExpires()));
    }

    private String produceJws(WebTokenDefinition webTokenDef, JwtClaims claims) throws Exception {
        JsonWebSignature jws = new JsonWebSignature();

        jws.setContentTypeHeaderValue("JWT");

        jws.setPayload(claims.toJson());

        if (webTokenDef.getAlgHeader() != null) {
            jws.setAlgorithmHeaderValue(webTokenDef.getAlgHeader());
        }

        KeyServiceProvider keyService = _serviceProvider.provideService(KeyServiceProvider.class);
        KeyVersion keyVersion = keyService.resolveKey(new KeyRefImpl(webTokenDef.getKey()));
        jws.setKey(keyVersion.getKey());
        jws.setKeyIdHeaderValue(keyVersion.getVersionPath());
        return jws.getCompactSerialization();
    }

    private String produceJwe(WebTokenDefinition webTokenDef, JwtClaims claims) throws Exception {
        JsonWebEncryption jwe = new JsonWebEncryption();

        jwe.setContentTypeHeaderValue("JWT");

        jwe.setPlaintext(claims.toJson());

        if (webTokenDef.getAlgHeader() != null) {
            jwe.setAlgorithmHeaderValue(webTokenDef.getAlgHeader());
        }

        if (webTokenDef.getContentEncryptionAlg() != null) {
            jwe.setEncryptionMethodHeaderParameter(webTokenDef.getContentEncryptionAlg());
        }

        if (isNonceRequired(webTokenDef)) {
            String nonce = getTokenGenerator(webTokenDef.getContentEncryptionAlg()).produceToken();
            byte[] iv = Base64.getDecoder().decode(nonce);
            jwe.setIv(iv);
        }

        KeyServiceProvider keyService = _serviceProvider.provideService(KeyServiceProvider.class);
        KeyVersion keyVersion = keyService.resolveKey(new KeyRefImpl(webTokenDef.getKey()));
        jwe.setKey(keyVersion.getKey());
        jwe.setKeyIdHeaderValue(keyVersion.getVersionPath());
        return jwe.getCompactSerialization();
    }

    @Override
    public String produceWebToken(String tokenType, Map<String, Object> userClaims) throws TokenizerException {
        assertReady();

        WebTokenDefinition webTokenDef = getWebTokenDefinition(tokenType);
        try {

            JwtClaims claims = new JwtClaims();
            claims.setIssuer(webTokenDef.getIssuer());
            claims.setAudience(webTokenDef.getAudience());
            claims.setExpirationTimeMinutesInTheFuture(webTokenDef.getExpires());
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(webTokenDef.getNotBefore()); // time before which the token is not yet
                                                                             // valid
            userClaims.forEach((name, value) -> {
                if (!claims.hasClaim(name)) {
                    claims.setClaim(name, value);
                }
            });
            TokenMetaData.Type type = webTokenDef.getType();
            if (type == null) {
                throw new TokenizerException(
                        "Invalid or unspecified token type for " + tokenType + " token definition");
            }
            switch (type) {
            default:
            case JWT:
                return claims.toJson();
            case JWS:
                return produceJws(webTokenDef, claims);
            case JWE:
                return produceJwe(webTokenDef, claims);
            case OPAQUE:
                return produceOpaque(webTokenDef, claims);
            }
        } catch (Exception exception) {
            throw new TokenizerException("Failed to encrypt " + tokenType + " token", exception);
        }
    }

    private void validateAuthClaims(WebTokenDefinition webTokenDef, AuthClaims jwtClaims)
            throws TokenizerException, MalformedAuthClaimException {

        Date now = new Date();
        if (jwtClaims.getNotBefore().after(now)) {
            throw new TokenExpiredException("Invalid " + webTokenDef.getName() + " token: use before valid");
        }

        if (jwtClaims.getExpirationTime().before(now)) {
            throw new TokenExpiredException("Invalid " + webTokenDef.getName() + " token: expired");
        }

        if (!jwtClaims.getAudience().contains(webTokenDef.getAudience())) {
            throw new TokenizerException("Invalid " + webTokenDef.getName() + " token: audience not valid");
        }

        if (jwtClaims.getIssuer() == null || !jwtClaims.getIssuer().equals(webTokenDef.getIssuer())) {
            throw new TokenizerException("Invalid " + webTokenDef.getName() + " token: issuer not valid");
        }

        boolean audienceFound = false;
        if (jwtClaims.hasAudience()) {
            for (String audience : jwtClaims.getAudience()) {
                if (audience.equals(webTokenDef.getAudience())) {
                    audienceFound = true;
                    break;
                }
            }
        }
        if (!audienceFound) {
            throw new TokenizerException("Invalid " + webTokenDef.getName() + " token: audience not valid");
        }
    }

    private String consumeJwe(WebTokenDefinition webTokenDef, String token, Key key)
            throws JoseException, KeyProviderException, ServiceNotFoundException {
        assertReady();

        JsonWebEncryption jwe = new JsonWebEncryption();

        if (webTokenDef.getAlgHeader() != null) {
            AlgorithmConstraints algConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST,
                    webTokenDef.getAlgHeader());
            jwe.setAlgorithmConstraints(algConstraints);
        }

        if (webTokenDef.getContentEncryptionAlg() != null) {
            AlgorithmConstraints encConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST,
                    webTokenDef.getContentEncryptionAlg());

            jwe.setContentEncryptionAlgorithmConstraints(encConstraints);
        }

        // Set the compact serialization on new Json Web Encryption object
        jwe.setCompactSerialization(token);

        if (key == null) {
            String keyPath = webTokenDef.getKey();
            if (keyPath == null) {
                throw new IllegalArgumentException(
                        "Token definition " + webTokenDef.getName() + " does not define a key");
            }
            KeyServiceProvider keyService = _serviceProvider.provideService(KeyServiceProvider.class);
            if(!KeyManagementAlgorithmIdentifiers.DIRECT.equals(webTokenDef.getAlgHeader())) {
                keyPath += "/public";
            }
            KeyVersion keyVersion = keyService.resolveKey(new KeyRefImpl(keyPath));
            key = keyVersion.getKey();
        }
        jwe.setKey(key);

        return jwe.getPlaintextString(); // Decrypt the JWE
    }

    private String consumeJws(WebTokenDefinition webTokenDef, String token, Key key)
            throws JoseException, KeyProviderException, ServiceNotFoundException {
        JsonWebSignature jws = new JsonWebSignature();

        if (webTokenDef.getAlgHeader() != null) {
            AlgorithmConstraints algConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST,
                    webTokenDef.getAlgHeader());
            jws.setAlgorithmConstraints(algConstraints);
        }

        // Set the compact serialization on new Json Web Encryption object
        jws.setCompactSerialization(token);

        if (key == null) {
            String keyPath = webTokenDef.getKey();
            if (keyPath == null) {
                throw new IllegalArgumentException(
                        "Token definition " + webTokenDef.getName() + " does not define a key");
            }
            KeyServiceProvider keyService = _serviceProvider.provideService(KeyServiceProvider.class);

            KeyVersion keyVersion = keyService.resolveKey(new KeyRefImpl(keyPath + "/public"));
            key = keyVersion.getKey();
        }
        jws.setKey(key);

        return jws.getPayload(); // Decrypt the JWS
    }

    @Override
    public AuthClaims consumeWebToken(String tokenType, String token) throws TokenizerException {
        assertReady();

        WebTokenDefinition webTokenDef = getWebTokenDefinition(tokenType);
        try {
            String plainTextToken;
            TokenMetaData.Type type = webTokenDef.getType();
            if (type == null) {
                throw new TokenizerException(
                        "Invalid or unspecified token type for " + tokenType + " token definition");
            }
            switch (type) {
            default:
            case JWT:
                plainTextToken = token;
                break;
            case JWS:
                plainTextToken = consumeJws(webTokenDef, token, null);
                break;
            case JWE:
                plainTextToken = consumeJwe(webTokenDef, token, null);
                break;
            case OPAQUE:
                plainTextToken = consumeOpaqueToken(token, String.class);
                break;
            }

            AuthClaims jwtClaims = AuthClaimsImpl.parse(plainTextToken);

            validateAuthClaims(webTokenDef, jwtClaims);

            return jwtClaims;

        } catch (KeyProviderException | MalformedAuthClaimException | ServiceNotFoundException
                | JoseException exception) {
            throw new TokenizerException("Invalid " + tokenType + " token", exception);
        }
    }

    @Override
    public AuthClaims consumeWebTokenWithJwk(String tokenType, String token, String jwk) throws TokenizerException {
        assertReady();

        WebTokenDefinition webTokenDef = getWebTokenDefinition(tokenType);

        try {
            String plainTextToken;
            Key key = JsonWebKey.Factory.newJwk(jwk).getKey();
            TokenMetaData.Type type = webTokenDef.getType();
            if (type == null) {
                throw new TokenizerException(
                        "Invalid or unspecified token type for " + tokenType + " token definition");
            }
            switch (type) {
            default:
            case JWT:
                plainTextToken = token;
                break;
            case JWS:
                plainTextToken = consumeJws(webTokenDef, token, key);
                break;
            case JWE:
                plainTextToken = consumeJwe(webTokenDef, token, key);
                break;
            case OPAQUE:
                plainTextToken = consumeOpaqueToken(token, String.class);
                break;
            }

            AuthClaims jwtClaims = AuthClaimsImpl.parse(plainTextToken);

            validateAuthClaims(webTokenDef, jwtClaims);

            return jwtClaims;

        } catch (KeyProviderException | MalformedAuthClaimException | ServiceNotFoundException
                | JoseException exception) {
            throw new TokenizerException("Invalid " + tokenType + " token", exception);
        }
    }

    @Override
    public String getPublicJwk(String tokenType) throws TokenizerException {
        assertReady();

        try {
            WebTokenDefinition webTokenDef = getWebTokenDefinition(tokenType);
            String publicKeyPath = webTokenDef.getKey() + "/public";
            KeyServiceProvider keyService = _serviceProvider.provideService(KeyServiceProvider.class);
            KeyVersion keyVersion = keyService.resolveKey(new KeyRefImpl(publicKeyPath));
            JsonWebKey jwk = JsonWebKey.Factory.newJwk(keyVersion.getKey());
            jwk.setKeyId(publicKeyPath);
            return jwk.toJson();
        } catch (JoseException | KeyProviderException | ServiceNotFoundException exception) {
            throw new TokenizerException("Invalid token type", exception);
        }
    }

    @Override
    public long removeAllExpired() throws TokenizerException {
        assertDatastoreReady();

        Query expiresBy = this.tokenCollection.buildQuery().root().property("expires").lessThan(new Date()).build();
        DatastoreCursor<TokenContainer> expiredContainers = this.tokenCollection.find(expiresBy);
        if (expiredContainers == null) {
            return 0;
        } else {
            int countRemoved = 0;
            for (TokenContainer expiredContainer : expiredContainers.toArray(TokenContainer[]::new)) {
                this.tokenCollection.remove(expiredContainer);
                countRemoved++;
            }
            return countRemoved;
        }
    }
}