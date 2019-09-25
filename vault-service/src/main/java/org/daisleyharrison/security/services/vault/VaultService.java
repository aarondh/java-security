package org.daisleyharrison.security.services.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.daisleyharrison.security.common.exceptions.AccessDeniedVaultException;
import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.exceptions.InvalidTokenVaultException;
import org.daisleyharrison.security.common.exceptions.VaultException;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.spi.VaultServiceProvider;
import org.daisleyharrison.security.common.utilities.StageImpl;
import org.daisleyharrison.security.services.vault.models.PrincipleClaims;
import org.daisleyharrison.security.services.vault.models.PrincipleClaimsForPath;
import org.daisleyharrison.security.services.vault.models.TokenClaims;
import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.services.vault.models.Capability;
import org.daisleyharrison.security.services.vault.models.CypherMetaData;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorProvider;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorTemplate;
import org.daisleyharrison.security.common.models.key.CachedKeyProvider;
import org.daisleyharrison.security.common.models.key.KeyProvider;
import org.daisleyharrison.security.common.models.key.KeyProvider.KeyVersion;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.services.vault.models.NamespaceMetaData;
import org.daisleyharrison.security.services.vault.models.Policy;
import org.daisleyharrison.security.services.vault.models.Principle;
import org.daisleyharrison.security.services.vault.models.VaultMetaData;
import org.daisleyharrison.security.services.key.internal.CachedKeyProviderImpl;
import org.daisleyharrison.security.services.cypher.cypherProvider.CypherProviderImpl;
import org.daisleyharrison.security.services.vault.utilities.Glob;
import org.daisleyharrison.security.services.vault.utilities.KeyGeneratorProviderImpl;
import org.daisleyharrison.security.services.key.internal.KeyStoreKeyProviderImpl;
import org.daisleyharrison.security.services.vault.utilities.PathUtils;
import org.daisleyharrison.security.services.vault.utilities.Template;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

public final class VaultService implements VaultServiceProvider {
    private static Logger LOGGER = LoggerFactory.getLogger(VaultService.class);
    private static final String SECURE_FILE_EXTENSION = ".sec";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String META_PATH = ".vault";
    private static final String SYS_PATH = "sys/";
    private static final String TOKEN_PATH = SYS_PATH + "token/";
    private static final String REVOKED_TOKEN_PATH = SYS_PATH + "token/.revoked/";
    private static final String POLICY_PATH = SYS_PATH + "policy/";
    private static final String PRINCIPLE_PATH = SYS_PATH + "principle/";
    private static final String VAULT_METADATA_FILENAME = "vault.json";
    private static final String NAMESPACE_METADATA_FILENAME = "namespace.json";
    private static final String TOKEN_ISSUER = "vault-service";
    private static final String TOKEN_AUDIENCE = TOKEN_ISSUER;
    private static final String ROOT_PRINCIPLE_NAME = "root";
    private static final String ROOT_POLICY_NAME = "root";
    private static final String VAULT_KEY_PATH_PREFIX = ".vault/";

    public enum State {
        CREATED, INITIALIZING, INITIALIZED, COMPROMISED, CLOSED, ERROR
    }

    private State state;
    private Path rootPath;
    private KeyStore keyStore;
    private VaultMetaData vaultMetaData;
    private NamespaceMetaData masterNamespace;
    private Principle rootPrinciple;
    private Map<Path, NamespaceMetaData> namespaceByPath = new HashMap<>();
    private Map<String, Policy> policiesByName = new HashMap<>();
    private KeyGeneratorProvider keyGeneratorProvider;
    private CypherProviderImpl cypherProvider;
    private KeyProvider keyProvider;
    private CachedKeyProvider cachedKeyProvider;

    @Override
    public boolean isInitialized() {
        return state != State.CREATED && state != State.CLOSED && state != State.INITIALIZING;
    }

    @Override
    public boolean isReady() {
        return state == State.INITIALIZED;
    }

    private void assertReady() {
        if (!isReady()) {
            throw new IllegalStateException("vault-service is in state " + state.toString() + " and is not ready");
        }
    }

    private void assertInitializing() {
        if (state != State.INITIALIZING) {
            throw new IllegalStateException(
                    "vault-service is in state " + state.toString() + " and is not initializing");
        }
    }

    public VaultService() {
        this.state = State.CREATED;
    }

    public Stage beginInitialize() {
        if (this.state != State.CREATED && this.state != State.CLOSED) {
            throw new IllegalStateException("vault-service is in state " + state.toString() + " and cannot initalize");
        }
        this.state = State.INITIALIZING;
        return new StageImpl(() -> {
            if (this.state != State.INITIALIZING) {
                throw new IllegalStateException(
                        "vault-service is in state " + state.toString() + " and was not initializing");
            }
            this.state = State.INITIALIZED;
        });
    }

    private Path toRelativePath(Path absolutePath) {
        return this.rootPath.relativize(absolutePath);
    }

    private static final String NEW_VAULT_TEMPLATE_RESOURCE = "org/daisleyharrison/security/services/vault/templates/new.json";

    @Override
    public void setRootPath(String rootPath) {
        this.rootPath = Path.of(rootPath);
    }

    @Override
    public void configure() throws Exception {
        assertInitializing();
        if (rootPath == null) {
            throw new IllegalArgumentException("rootPath was not set (call setRootPath() first)");
        }
        try {
            try (InputStream templateStream = this.getClass().getClassLoader()
                    .getResourceAsStream(NEW_VAULT_TEMPLATE_RESOURCE)) {
                Template template = new Template(templateStream);
                template.apply(this.rootPath, Template.Action.KEEP);
            } catch (IOException | IllegalArgumentException exception) {

            }

            Path metaDataPath = this.rootPath.resolve(META_PATH);
            Path metaDataFilePath = metaDataPath.resolve(VAULT_METADATA_FILENAME);
            try (InputStream input = Files.newInputStream(metaDataFilePath, StandardOpenOption.READ)) {
                vaultMetaData = objectMapper.readValue(input, VaultMetaData.class);
            }

            this.keyGeneratorProvider = new KeyGeneratorProviderImpl(
                    "org.daisleyharrison.security.services.vault.keyGenerators");

            this.cypherProvider = new CypherProviderImpl(
                    "org.daisleyharrison.security.services.key.cypherProvider.cyphers");

            char[] keystorePassword = vaultMetaData.getKeyStore().getPassword();
            Path keyStorePath = metaDataPath.resolve(vaultMetaData.getKeyStore().getPath());
            if (Files.exists(keyStorePath) && Files.size(keyStorePath) > 0) {
                this.keyStore = getKeyStore(keyStorePath, vaultMetaData.getKeyStore().getType(), keystorePassword);
            } else {
                if (Files.exists(keyStorePath)) {
                    // remove the 0 length keystore created by the template
                    Files.delete(keyStorePath);
                }
                this.keyStore = createKeyStore(keyStorePath, vaultMetaData.getKeyStore().getType(), keystorePassword);
            }

            this.keyProvider = new KeyStoreKeyProviderImpl(VAULT_KEY_PATH_PREFIX, keyStore, keystorePassword);

            this.cachedKeyProvider = new CachedKeyProviderImpl(keyProvider,
                    Duration.ofSeconds(vaultMetaData.getKeyTTL()));

            this.cypherProvider.setKeyProvider(this.cachedKeyProvider);

            NamespaceMetaData tempMasterNamespace = new NamespaceMetaData();
            tempMasterNamespace.setCypher(vaultMetaData.getMasterCypher());
            namespaceByPath.put(null, tempMasterNamespace);

            masterNamespace = readNamespace(null, true);
            namespaceByPath.put(null, masterNamespace);

            rootPrinciple = createTempRootPrinciple(30);
            rootPrinciple = readRootPrinciple();

            secureAll(rootPrinciple);

        } catch (Exception exception) {
            this.state = State.ERROR;
            throw exception;
        }
    }

    private NamespaceMetaData readNamespace(Path path, boolean allowUnsecureRead) throws CypherException, IOException {
        Path absolutePath = path == null ? this.rootPath : this.rootPath.resolve(path);
        Path metaDataPath = absolutePath.resolve(NAMESPACE_METADATA_FILENAME + SECURE_FILE_EXTENSION);
        if (Files.exists(metaDataPath)) {
            String secureMetaData = Files.readString(metaDataPath);
            NamespaceMetaData namespace;
            if (path == null) {
                namespace = getNamespace(path);
            } else {
                namespace = getNamespace(path.getParent());
            }
            return objectMapper.readValue(decrypt(secureMetaData, namespace), NamespaceMetaData.class);
        } else if (allowUnsecureRead) {
            metaDataPath = absolutePath.resolve(NAMESPACE_METADATA_FILENAME);
            if (Files.exists(metaDataPath)) {
                String unsecureMetaData = Files.readString(metaDataPath);
                return objectMapper.readValue(unsecureMetaData, NamespaceMetaData.class);
            }
        }
        return null;
    }

    private NamespaceMetaData getNamespace(Path path) throws CypherException, IOException {
        NamespaceMetaData namespace = namespaceByPath.get(path);
        if (namespace == null) {
            namespace = readNamespace(path, false);
            if (namespace != null) {
                if (path != null) {
                    namespace.setParent(getNamespace(path.getParent()));
                }
                namespaceByPath.put(path, namespace);
            } else if (path == null) {
                return null;
            } else {
                namespace = getNamespace(path.getParent());
                namespaceByPath.put(path, namespace);
            }
        }
        return namespace;
    }

    private Path secretPathToFilePath(String secretPath) {
        return this.rootPath.resolve(secretPath + SECURE_FILE_EXTENSION);
    }

    private Path secretPathToNonSecretFilePath(String secretPath) {
        return this.rootPath.resolve(secretPath);
    }

    private String filePathToSecretPath(Path path) {
        return toRelativePath(path).toString();
    }

    private void secureAll(Principle principle) throws IOException {

        Glob allUnsecureFilePaths = new Glob(this.rootPath, "regex:.+(?<!\\" + SECURE_FILE_EXTENSION + ")$");
        allUnsecureFilePaths.forEach(path -> {
            try {
                String secretPath = filePathToSecretPath(path);
                if (secretPath.startsWith(META_PATH)) {
                    LOGGER.info("Skipping securing file: {}", path);
                } else {
                    LOGGER.info("Securing file: {}", path);
                    String unsecure = Files.readString(path);
                    writeSecret(principle, secretPath, unsecure);
                    Files.delete(path);
                }
            } catch (IOException | CypherException exception) {
                LOGGER.error("Failed to secure secret {}: {}", PathUtils.removeExtension(path.toString()),
                        exception.getMessage());
            }
        });
    }

    private void createKey(KeyStore keyStore, CypherMetaData cypherMetaData, char[] keyStorePassword)
            throws CypherException {
        if (cypherMetaData != null) {
            KeyGeneratorTemplate keyGenerator = keyGeneratorProvider.getKeyGenerator(cypherMetaData);
            if (cypherMetaData.getIssuer() == null) {
                cypherMetaData.setIssuer(vaultMetaData.getIssuerDN());
            }
            if (cypherMetaData.getSubject() == null) {
                cypherMetaData.setSubject(vaultMetaData.getSubjectDN());
            }
            keyGenerator.generateKeys(keyStore, cypherMetaData);
        }
    }

    private KeyStore createKeyStore(Path path, String keyStoreType, char[] password) throws FileNotFoundException,
            IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, CypherException {

        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            KeyStore newKeyStore = KeyStore.getInstance(keyStoreType);
            newKeyStore.load(null, null);

            createKey(newKeyStore, vaultMetaData.getMasterCypher(), password);

            createKey(newKeyStore, vaultMetaData.getTokenSignatureCypher(), password);

            newKeyStore.store(outputStream, password);
            return newKeyStore;
        }
    }

    private KeyStore getKeyStore(Path path, String keyStoreType, char[] password) throws FileNotFoundException,
            IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        if (keyStoreType == null) {
            throw new IllegalArgumentException("keyStoreType cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("password cannot be null");
        }
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(inputStream, password);
            return keyStore;
        }
    }

    public String encrypt(String secureText, NamespaceMetaData namespaceMetaData) throws CypherException {
        CypherMetaData cypherMetaData = namespaceMetaData.getCypher();
        if (cypherMetaData != null) {
            StringCypher cypher = cypherProvider.getStringCypher(cypherMetaData.toCypherSpec(),
                    cypherMetaData.toKeySpec());
            return cypher.encrypt(secureText);
        }
        throw new IllegalArgumentException("namespaceMetaData does not have a cypher defined.");
    }

    private String decrypt(String secureText, NamespaceMetaData namespaceMetaData) throws CypherException {
        CypherMetaData cypherMetaData = namespaceMetaData.getCypher();
        if (cypherMetaData != null) {
            StringCypher cypher = cypherProvider.getStringCypher(cypherMetaData.toCypherSpec(),
                    cypherMetaData.toKeySpec());
            return cypher.decrypt(secureText);
        }
        throw new IllegalArgumentException("namespaceMetaData does not have a cypher defined.");
    }

    private String readSecret(Principle principle, String path) throws AccessDeniedVaultException {
        if (principle.hasCapability(Capability.READ)) {
            Path filePath = secretPathToFilePath(path);
            Path namespacePath = Path.of(path).getParent();
            if (path.endsWith(NAMESPACE_METADATA_FILENAME)) {
                namespacePath = namespacePath.getParent();
            }
            try {
                String secureSecret = Files.readString(filePath);

                return decrypt(secureSecret, getNamespace(namespacePath));
            } catch (CypherException | IOException exception) {
                LOGGER.error("Error reading secret " + path, exception);
                throw new AccessDeniedVaultException();
            }
        } else {
            throw new AccessDeniedVaultException();
        }
    }

    private <T> T readSecret(Principle principle, String path, Class<T> type) throws VaultException {
        String unsecureSecret = readSecret(principle, path);
        try {
            if (type == String.class) {
                return type.cast(unsecureSecret);
            } else if (type == PrincipleClaims.class) {
                return type.cast(PrincipleClaims.parse(unsecureSecret));
            } else if (type == TokenClaims.class) {
                return type.cast(TokenClaims.parse(unsecureSecret));
            } else {
                return objectMapper.readValue(unsecureSecret, type);
            }
        } catch (IOException | InvalidJwtException | ClassCastException exception) {
            throw new VaultException(exception.getMessage(), exception);
        }

    }

    private static final OpenOption[] UPDATE_OPTIONS = new OpenOption[] { StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING };
    private static final OpenOption[] CREATE_OPTIONS = new OpenOption[] { StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING };

    private void writeSecretString(Principle principle, String path, String unsecureSecret)
            throws CypherException, IOException, AccessDeniedVaultException {
        Path filePath = secretPathToFilePath(path);
        Path namespacePath = Path.of(path).getParent();
        if (path.endsWith(NAMESPACE_METADATA_FILENAME)) {
            namespacePath = namespacePath == null ? null : namespacePath.getParent();
        }
        String secureSecret = encrypt(unsecureSecret, getNamespace(namespacePath));
        Path dirPath = filePath.getParent();
        if (!Files.exists(dirPath)) {
            if (principle.hasCapability(Capability.MANAGE)) {
                Files.createDirectories(dirPath);
            }
        }
        OpenOption[] openOptions = null;
        if (principle.hasCapability(Capability.MANAGE)) {
            openOptions = CREATE_OPTIONS;
        } else if (principle.hasCapability(Capability.CREATE)) {
            openOptions = CREATE_OPTIONS;
        } else if (principle.hasCapability(Capability.UPDATE)) {
            openOptions = UPDATE_OPTIONS;
        } else {
            throw new AccessDeniedVaultException();
        }

        Files.writeString(filePath, secureSecret, openOptions);
    }

    private void writeSecret(Principle principle, String path, Object unsecureSecret)
            throws CypherException, IOException, AccessDeniedVaultException {
        if (unsecureSecret instanceof String) {
            writeSecretString(principle, path, (String) unsecureSecret);
        } else {
            writeSecretString(principle, path, objectMapper.writeValueAsString(unsecureSecret));
        }
    }

    private void deleteSecret(Principle principle, String path) throws AccessDeniedVaultException {
        if (principle.hasCapability(Capability.DELETE)) {
            Path filePath = secretPathToFilePath(path);
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                } else {
                    throw new AccessDeniedVaultException();
                }
            } catch (IOException exception) {
                throw new AccessDeniedVaultException();
            }
        } else {
            throw new AccessDeniedVaultException();
        }
    }

    private String toTokenSecretPath(String token) {
        return TOKEN_PATH + token;
    }

    private String toPolicySecretPath(String policy) {
        return POLICY_PATH + policy;
    }

    private String toRevokedTokenSecretPath(String token) {
        return REVOKED_TOKEN_PATH + token;
    }

    private String createJWT(TokenClaims claims, CypherMetaData signatureCypher)
            throws JoseException, IOException, GeneralSecurityException, KeyProviderException {

        JsonWebSignature jws = new JsonWebSignature();

        // The payload of the JWS is JSON content of the JWT Claims
        jws.setPayload(claims.toJson());

        // The JWT is signed using the private key
        KeyProvider.KeyVersion keyVersion = keyProvider.resolveKey(signatureCypher.toKeySpec());
        jws.setKey(keyVersion.getKey(PrivateKey.class));

        // Set the signature algorithm on the JWT/JWS that will integrity protect the
        // claims
        jws.setAlgorithmHeaderValue(signatureCypher.getAlgorithm());

        // Sign the JWS and produce the compact serialization or the complete JWT/JWS
        // representation, which is a string consisting of three dot ('.') separated
        // base64url-encoded parts in the form Header.Payload.Signature
        // If you wanted to encrypt it, you can simply set this jwt as the payload
        // of a JsonWebEncryption object and set the cty (Content Type) header to "jwt".
        return jws.getCompactSerialization();
    }

    private String createToken(Principle principle, String scope, int ttl, int uses, boolean opaque, boolean renewable)
            throws JoseException, MalformedClaimException, IOException, CypherException, AccessDeniedVaultException,
            KeyProviderException, GeneralSecurityException {
        TokenClaims claims = new TokenClaims();
        claims.setIssuedAtToNow();
        claims.setIssuer(TOKEN_ISSUER);
        claims.setAudience(TOKEN_AUDIENCE);
        claims.setGeneratedJwtId();
        claims.setExpirationTimeMinutesInTheFuture(ttl);
        if (uses > 0) {
            claims.setUses(uses);
        }
        claims.setSubject(toPrincipleSecretPath(principle.getName()));
        if (renewable) {
            claims.setRenewable(renewable);
        }
        if (scope != null) {
            claims.addScope(scope);
        }
        String token = claims.toOpaqueToken();
        if (opaque) {
            String tokenSecretPath = toTokenSecretPath(token);
            writeSecret(this.rootPrinciple, tokenSecretPath, claims.toJson());
            return token;
        } else {
            if (vaultMetaData.getTokenSignatureCypher() == null) {
                return token;
            } else {
                return createJWT(claims, vaultMetaData.getTokenSignatureCypher());
            }
        }
    }

    private boolean isRevokedToken(String token) {
        Path revokedTokenPath = secretPathToFilePath(toRevokedTokenSecretPath(token));
        return Files.exists(revokedTokenPath);
    }

    private TokenClaims readToken(String token) throws VaultException {
        if (isRevokedToken(token)) {
            throw new InvalidTokenVaultException();
        }
        String tokenSecretPath = toTokenSecretPath(token);
        return readSecret(this.rootPrinciple, tokenSecretPath, TokenClaims.class);
    }

    private Policy readPolicy(String policyName) throws VaultException {
        String policySecretPath = toPolicySecretPath(policyName);
        return readSecret(this.rootPrinciple, policySecretPath, Policy.class);
    }

    private Policy getPolicy(String policyName) throws VaultException {
        Policy policy = policiesByName.get(policyName);
        if (policy == null) {
            policy = readPolicy(policyName);
            policiesByName.put(policyName, policy);
        }
        return policy;
    }

    private String toPrincipleSecretPath(String principleId) {
        if (principleId.contains("/") || principleId.contains("\\")) {
            return principleId; // already fully qualified path
        }
        return PRINCIPLE_PATH + principleId;
    }

    private Policy computePolicyForPath(PrincipleClaims claims, Path path) throws VaultException {
        if (!path.startsWith("\\")) {
            path = Path.of("\\").resolve(path);
        }
        String lastPolicyName = null;
        try {
            Policy combined = null;
            for (String policyName : claims.getPolicies()) {
                lastPolicyName = policyName;
                Policy policy = getPolicy(policyName);
                if (policy == null) {
                    LOGGER.info("Missing policy '{}' for principle '{}'", policyName, claims.getSubject());
                    return Policy.DENY_ALL;
                }
                if (policy.applysToPath(path)) {
                    if (combined == null) {
                        combined = policy;
                    } else {
                        combined = combined.combine(policy);
                    }
                }
            }
            return combined == null ? Policy.DENY_ALL : combined;
        } catch (MalformedClaimException | VaultException exception) {
            try {
                if (lastPolicyName == null) {
                    LOGGER.error("Error getting policies for principle " + claims.getSubject(), exception);
                }
                LOGGER.error("Error computing policy '{}' for principle '{}' on path {}", lastPolicyName,
                        claims.getSubject(), path);
            } catch (MalformedClaimException exception2) {
                LOGGER.error("Error getting policies for path " + path.toString(), exception2);
            }
            return Policy.DENY_ALL;
        }
    }

    /**
     * Used only during initialization
     * 
     * @param ttleInMinutes
     * @return
     */
    private Principle createTempRootPrinciple(int ttleInMinutes) throws MalformedClaimException {
        PrincipleClaims claims = new PrincipleClaims();
        claims.addPolicy(ROOT_POLICY_NAME);
        claims.setIssuedAtToNow();
        claims.setIssuer(TOKEN_ISSUER);
        claims.setAudience(TOKEN_AUDIENCE);
        claims.setClaim(PrincipleClaims.ReservedClaims.PREFERRED_USERNAME, ROOT_PRINCIPLE_NAME);
        claims.setExpirationTimeMinutesInTheFuture(ttleInMinutes);
        return new PrincipleClaimsForPath(claims, Path.of("/"), Policy.ALLOW_ALL);
    }

    private Principle readRootPrinciple() throws IOException, GeneralSecurityException, VaultException {
        assertInitializing();
        PrincipleClaims root = null;
        Path principleFilePath = secretPathToFilePath(toPrincipleSecretPath(ROOT_PRINCIPLE_NAME));
        if (Files.exists(principleFilePath)) {
            root = readPrinciple(this.rootPrinciple, ROOT_PRINCIPLE_NAME);
        } else {
            principleFilePath = secretPathToNonSecretFilePath(toPrincipleSecretPath(ROOT_PRINCIPLE_NAME));
            if (Files.exists(principleFilePath)) {
                String content = Files.readString(principleFilePath);
                try {
                    root = PrincipleClaims.parse(content);
                } catch (InvalidJwtException exception) {
                    throw new VaultException(exception.getMessage(), exception);
                }
            } else {
                throw new VaultException("Missing root principle");
            }
        }
        return new PrincipleClaimsForPath(root, Path.of("/"), Policy.ALLOW_ALL);
    }

    private PrincipleClaims readPrinciple(Principle principle, String principleId) throws VaultException {
        String principleSecretPath = toPrincipleSecretPath(principleId);
        return readSecret(principle, principleSecretPath, PrincipleClaims.class);
    }

    private void writeToken(Principle principle, TokenClaims claims)
            throws IOException, CypherException, MalformedClaimException, AccessDeniedVaultException {
        String tokenSecretPath = toTokenSecretPath(claims.toOpaqueToken());
        writeSecret(principle, tokenSecretPath, claims.toJson());
    }

    private void revokeToken(Principle principle, TokenClaims claims, boolean ensureDeletion) throws IOException,
            GeneralSecurityException, MalformedClaimException, AccessDeniedVaultException, CypherException {
        String token = claims.toOpaqueToken();
        String revokedTokenSecretPath = toRevokedTokenSecretPath(token);
        writeSecret(principle, revokedTokenSecretPath, claims.toJson());
        String tokenSecretPath = toTokenSecretPath(token);
        try {
            deleteSecret(principle, tokenSecretPath);
        } catch (AccessDeniedVaultException exception) {
            if (ensureDeletion) {
                throw exception;
            }
        }
    }

    private TokenClaims validateJwt(String jwt, CypherMetaData signatureCypher)
            throws IOException, GeneralSecurityException, InvalidTokenVaultException, KeyProviderException {

        KeyVersion keyVersion = keyProvider.resolveKey(signatureCypher.toKeySpec());
        PublicKey publicKey = keyVersion.getKey(PublicKey.class);
        // Use JwtConsumerBuilder to construct an appropriate JwtConsumer, which will
        // be used to validate and process the JWT.
        // The specific validation requirements for a JWT are context dependent,
        // however,
        // it typically advisable to require a (reasonable) expiration time, a trusted
        // issuer, and
        // and audience that identifies your system as the intended recipient.
        // If the JWT is encrypted too, you need only provide a decryption key or
        // decryption key resolver to the builder.
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime() // the JWT must have an expiration
                                                                                      // time
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for
                                                  // clock skew
                .setRequireSubject() // the JWT must have a subject claim
                .setExpectedIssuer(TOKEN_ISSUER) // whom the JWT needs to have been issued by
                .setExpectedAudience(TOKEN_AUDIENCE) // to whom the JWT is intended for
                .setVerificationKey(publicKey) // verify the signature with the public
                                               // key
                .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the given context
                        new AlgorithmConstraints(ConstraintType.WHITELIST, // which is only RS256 here
                                signatureCypher.getAlgorithm()))
                .build(); // create the JwtConsumer instance

        try {
            // Validate the JWT and process it to the Claims
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            TokenClaims tokenClaims = new TokenClaims(jwtClaims);
            if (isRevokedToken(tokenClaims.toOpaqueToken())) {
                throw new InvalidTokenVaultException("revoked");
            } else {
                if (tokenClaims.hasUses()) {
                    int uses = tokenClaims.getUses();
                    if (uses == 1) {
                        revokeToken(this.rootPrinciple, tokenClaims, false);
                    } else {
                        TokenClaims savedClaims = readToken(tokenClaims.toOpaqueToken());
                        if (savedClaims == null) {
                            savedClaims = tokenClaims;
                        }
                        savedClaims.setUses(savedClaims.getUses() - 1);
                        writeToken(this.rootPrinciple, savedClaims);
                    }
                }
                return tokenClaims;
            }
        } catch (VaultException | MalformedClaimException | CypherException | InvalidJwtException exception) {
            throw new InvalidTokenVaultException(exception.getMessage(), exception);
        }
    }

    private TokenClaims validateOpaqueToken(String token) throws VaultException {
        try {
            TokenClaims claims = readToken(token);
            if (claims == null) {
                throw new InvalidTokenVaultException();
            }
            if (claims.isExpired()) {
                revokeToken(this.rootPrinciple, claims, true);
                throw new InvalidTokenVaultException();
            }

            if (claims.hasUses()) {
                int uses = claims.getUses();
                if (uses == 1) {
                    claims.setUses(0);
                    revokeToken(this.rootPrinciple, claims, true);
                } else {
                    claims.setUses(uses - 1);
                    writeToken(this.rootPrinciple, claims);
                }
            }

            return claims;

        } catch (IOException | CypherException | GeneralSecurityException | MalformedClaimException exception) {
            throw new InvalidTokenVaultException();
        }
    }

    private PrincipleClaims validateToken(String token, Path path) throws VaultException {
        try {
            TokenClaims claims;
            if (TokenClaims.isOpaqueToken(token)) {
                claims = validateOpaqueToken(token);
            } else {
                claims = validateJwt(token, vaultMetaData.getTokenSignatureCypher());
            }
            if (claims == null) {
                throw new InvalidTokenVaultException();
            }

            // if the path is supplied and the token claims have scope defined
            // check to see if the path is in scope
            if (path != null) {
                if (claims.hasScope()) {
                    boolean isNotInScope = true;
                    for (String scope : claims.getScope()) {
                        if (path.startsWith(scope)) {
                            isNotInScope = false;
                            break;
                        }
                    }
                    if (isNotInScope) {
                        throw new InvalidTokenVaultException();
                    }
                }
            }

            return readPrinciple(this.rootPrinciple, claims.getSubject());

        } catch (IOException | GeneralSecurityException | MalformedClaimException | KeyProviderException exception) {
            throw new InvalidTokenVaultException();
        }
    }

    private Principle validateTokenFor(String token, Path path) throws VaultException {
        PrincipleClaims claims = validateToken(token, path);
        Policy policy = computePolicyForPath(claims, path);
        return new PrincipleClaimsForPath(claims, path, policy);
    }

    @Override
    public CompletableFuture<String> readFromVault(String token, String path) {
        assertReady();
        return CompletableFuture.supplyAsync(() -> {
            try {
                Principle principle = validateTokenFor(token, Path.of(path));
                return readSecret(principle, path);
            } catch (VaultException exception) {
                LOGGER.error("Error reading secret {}: {}", path, exception.getMessage());
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeFromVault(String token, String path) {
        assertReady();
        return CompletableFuture.runAsync(() -> {
            try {
                Principle principle = validateTokenFor(token, Path.of(path));
                deleteSecret(principle, path);
            } catch (VaultException exception) {
                LOGGER.error("Error deleting secret {}: {}", path, exception.getMessage());
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public CompletableFuture<Void> writeToVault(String token, String path, String unsecureSecret) {
        assertReady();
        return CompletableFuture.runAsync(() -> {
            try {
                Principle principle = validateTokenFor(token, Path.of(path));
                writeSecret(principle, path, unsecureSecret);
            } catch (VaultException | CypherException | IOException exception) {
                LOGGER.error("Error writing secret {}: {}", path, exception.getMessage());
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public CompletableFuture<Void> revokeVaultToken(String token, String tokenToRevoke) {
        return CompletableFuture.runAsync(() -> {
            try {
                Principle principle = validateTokenFor(token, Path.of(TOKEN_PATH));
                boolean isOpaqueToken = TokenClaims.isOpaqueToken(tokenToRevoke);
                TokenClaims claimsToRevoke;
                if (isOpaqueToken) {
                    claimsToRevoke = readToken(tokenToRevoke);
                } else {
                    claimsToRevoke = validateJwt(tokenToRevoke, vaultMetaData.getTokenSignatureCypher());
                }
                if (claimsToRevoke != null) {
                    revokeToken(principle, claimsToRevoke, isOpaqueToken);
                }
            } catch (VaultException | CypherException | MalformedClaimException | GeneralSecurityException
                    | IOException exception) {
                LOGGER.error("Error revoking token {}: {}", tokenToRevoke, exception.getMessage());
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public CompletableFuture<Void> revokeVaultToken(String tokenToRevoke) {
        return revokeVaultToken(tokenToRevoke, tokenToRevoke);
    }

    @Override
    public CompletableFuture<String> renewVaultToken(String token, String tokenToRenew) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Principle principle = validateTokenFor(token, Path.of(TOKEN_PATH));
                TokenClaims claimsToRenew = readToken(tokenToRenew);
                if (claimsToRenew.isRenewable()) {
                    if (claimsToRenew.getExpirationTime().isBefore(NumericDate.now())) {
                        throw new IllegalArgumentException();
                    }
                    writeToken(principle, claimsToRenew);
                    return tokenToRenew;
                }
                throw new IllegalArgumentException();
            } catch (VaultException | MalformedClaimException | CypherException | IOException exception) {
                LOGGER.error("Error revoking token {}: {}", tokenToRenew, exception.getMessage());
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public CompletableFuture<String> renewVaultToken(String tokenToRenew) {
        return renewVaultToken(tokenToRenew, tokenToRenew);
    }

    @Override
    public CompletableFuture<String> createVaultToken(String token, String scope, int uses, int ttl, boolean opaque,
            boolean renewable) {
        return CompletableFuture.supplyAsync(() -> {
            Principle principle = null;
            try {
                Path path = Path.of(scope);
                principle = validateTokenFor(token, path);
                return createToken(principle, scope, ttl, uses, opaque, renewable);
            } catch (VaultException | CypherException | JoseException | MalformedClaimException
                    | GeneralSecurityException | IOException exception) {
                if (principle == null) {
                    LOGGER.error("Error creating token of scope {} for token {}: {}", scope, token,
                            exception.getMessage());
                } else {
                    LOGGER.error("Error creating token of scope {} for principle {}: {}", scope, principle.getId(),
                            exception.getMessage());
                }
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public CompletableFuture<String> authenticate(String principleId, char[] password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PrincipleClaims principle = readPrinciple(this.rootPrinciple, principleId);
                if (principle == null) {
                    throw new SecurityException();
                }

                if (!principle.hasPassword(password)) {
                    throw new SecurityException();
                }

                return createToken(principle, null, vaultMetaData.getUserPrincipleTokenTTL(), Integer.MAX_VALUE, true,
                        true);

            } catch (VaultException | CypherException | JoseException | MalformedClaimException
                    | GeneralSecurityException | IOException exception) {
                LOGGER.error("Error authenicating principle {}: {}", principleId, exception.getMessage());
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public void close() {
        this.state = State.CLOSED;
    }

}