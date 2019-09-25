package org.daisleyharrison.security.services.cypher;

import org.daisleyharrison.security.common.spi.CypherServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.utilities.StageImpl;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.models.cypher.CypherFunc;
import org.daisleyharrison.security.common.models.Endorser;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.cypher.ConcealedString;
import org.daisleyharrison.security.common.models.cypher.CypherAction;
import org.daisleyharrison.security.common.models.cypher.CypherServiceState;
import org.daisleyharrison.security.common.models.cypher.CypherSpecification;
import org.daisleyharrison.security.common.models.cypher.HashCypher;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.models.key.CachedKeyProvider;
import org.daisleyharrison.security.common.models.key.FramedKeyProvider;
import org.daisleyharrison.security.common.models.key.KeyFrame;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.common.models.key.KeyProvider.KeyVersion;
import org.daisleyharrison.security.common.models.cypher.Cypher;
import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.services.cypher.cypherProvider.CypherProviderImpl;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherSpecImpl;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.KeyRefImpl;
import org.daisleyharrison.security.services.cypher.internal.*;
import org.daisleyharrison.security.services.cypher.keyProvider.FramedKeyProviderImpl;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.IllegalArgumentException;
import java.util.Arrays;

import javax.management.ServiceNotFoundException;

public class CypherService implements CypherServiceProvider {

    private static Logger LOGGER = LoggerFactory.getLogger(CypherService.class);

    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    private static final String CYPHER_CONFIG_PREFIX = "cypher.";
    private static final String CYPHERS_CONFIG_PREFIX = CYPHER_CONFIG_PREFIX + "cyphers.";

    // Salt and password hashing parameters

    private static final int MINIMUM_SALT_LENGTH = 10;

    // ConcealedString constants
    private static String CONCEALEDSTRING_DEFAULT_CHARSET = "UTF-8";
    private static int CONCEALEDSTRING_MAXIMUM_LENGTH = 65535;
    private static int CONCEALEDSTRING_SALT_LENGTH = 64;

    private SecureRandom secureRandom;
    private CypherProviderImpl cypherProvider;
    private ConfigurationServiceProvider config;
    private CypherServiceState state = CypherServiceState.CREATED;
    private CypherSpecification internalConcealCypher;
    private KeyReference internalConcealKeyRef;
    private CypherSpecification externalConcealCypher;
    private KeyReference externalConcealKeyRef;
    private CachedKeyProvider keyProvider;
    private FramedKeyProvider framedKeyProvider;
    private ReentrantLock stateLock;
    private static CypherServiceProvider s_instance;

    public static CypherServiceProvider getInstance() throws CypherException {
        if (s_instance == null) {
            s_instance = new CypherService();
        }
        return s_instance;
    }

    protected CypherService() throws CypherException {
        stateLock = new ReentrantLock();

        try {
            this.secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException exception1) {
            try {
                this.secureRandom = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException exception2) {
                LOGGER.error("Unable to load a secure random algorithm", exception2);
                throw new CypherException(exception2);
            }
        }
        this.state = CypherServiceState.CREATED;

        try (Stage stage = beginInitialize()) {
            // this.internalConcealSet = generateKeyPathSet();
        } catch (CypherException exception) {
            LOGGER.error("Unable to create the internal conceal KeyPair", exception);
            throw exception;
        } catch (Exception exception) {
            LOGGER.error("Unable to create the internal conceal KeyPair", exception);
            throw new CypherException(exception);
        } finally {
            this.state = CypherServiceState.CREATED;
        }
    }

    private void generateKeyPathSet() throws CypherException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException exception) {
            throw new CypherException(exception);
        }
    }

    private void ensureState(boolean compromised, CypherServiceState... requiredStates) throws IllegalStateException {
        this.stateLock.lock();
        try {
            for (CypherServiceState state : requiredStates) {
                if (this.state == state) {
                    return;
                }
            }

            CypherServiceState oldState = this.state;

            if (compromised) {
                this.state = CypherServiceState.COMPROMISED;
            }

            String expectedStates = "";
            for (CypherServiceState state : requiredStates) {
                if (!expectedStates.isEmpty()) {
                    expectedStates += ", ";
                }
                expectedStates += state.toString();
            }

            String message = String.format("CypherService state %s, expected state %s", oldState.toString(),
                    expectedStates);
            if (this.state != oldState) {
                message += String.format(", service now marked %s", this.state.toString());
            }
            throw new IllegalStateException(message);
        } finally {
            stateLock.unlock();
        }
    }

    private void ensureState(CypherServiceState... requiredStates) throws IllegalStateException {
        ensureState(false, requiredStates);
    }

    public KeyFrame openFrame() throws CypherException {
        ensureState(CypherServiceState.INITIALIZED);
        try {
            return this.framedKeyProvider.openFrame();
        } catch (KeyProviderException exception) {
            throw new CypherException(exception);
        }
    }

    @Override
    public boolean isInitialized() {
        this.stateLock.lock();
        try {
            return this.state != CypherServiceState.CREATED && this.state != CypherServiceState.INITIALIZING;
        } finally {
            this.stateLock.unlock();
        }
    }

    @Override
    public boolean isReady() {
        this.stateLock.lock();
        try {
            return this.state == CypherServiceState.INITIALIZED;
        } finally {
            this.stateLock.unlock();
        }
    }

    public synchronized Stage beginInitialize() {
        ensureState(true, CypherServiceState.CREATED);
        this.state = CypherServiceState.INITIALIZING;
        this.stateLock.lock();
        LOGGER.info("cypher-service initializing");
        StageImpl stage = new StageImpl(() -> {
            ensureState(true, CypherServiceState.INITIALIZING);
            this.state = CypherServiceState.INITIALIZED;
            LOGGER.info("cypher-service initialized");
            this.stateLock.unlock();
        });
        return stage;

    }

    @Override
    public void configure() throws CypherException {
        try {
            config = _serviceProvider.provideService(ConfigurationServiceProvider.class);
            this.cypherProvider = new CypherProviderImpl("org.daisleyharrison.security.services.cypher.cypherProvider.cyphers");
            this.keyProvider = (CachedKeyProvider)_serviceProvider.provideService(KeyServiceProvider.class);
            this.framedKeyProvider = new FramedKeyProviderImpl(keyProvider, false);
            this.cypherProvider.setKeyProvider(keyProvider);

            this.internalConcealCypher = getCypherSpec("internalConcealString");
            this.internalConcealKeyRef = getKeyRef("internalConcealString");
            this.externalConcealCypher = getCypherSpec("externalConcealString");
            this.externalConcealKeyRef = getKeyRef("externalConcealString");
        } catch (ServiceNotFoundException exception) {
            this.state = CypherServiceState.COMPROMISED;
            throw new CypherException("cypher-service failed to load configuration-service", exception);
        } catch (IllegalArgumentException exception) {
            this.state = CypherServiceState.COMPROMISED;
            throw new CypherException("cypher-service failed to load keystore", exception);
        }
    }

    private KeyReference getKeyRef(final String cypherName) {
        return new KeyRefImpl(config.getValue(CYPHERS_CONFIG_PREFIX + cypherName + ".keyPath"));
    }

    private CypherSpecification getCypherSpec(final String cypherName) {
        return new CypherSpecImpl(config.getValue(CYPHERS_CONFIG_PREFIX + cypherName + ".algorithm"));
    }

    private HashSpecification getHashSpec(final String cypherName) {
        String hashSpecPrefix = CYPHERS_CONFIG_PREFIX + cypherName;
        return new HashSpecification() {

            @Override
            public byte[] getSalt() {
                String salt = config.getValue(hashSpecPrefix + ".salt", null);
                return salt == null ? null : salt.getBytes();
            }

            @Override
            public int getKeyLength() {
                return config.getIntegerValue(hashSpecPrefix + ".keyLength", 0);
            }

            @Override
            public int getIterations() {
                return config.getIntegerValue(hashSpecPrefix + ".iterations", 0);
            }

            @Override
            public String getAlgorithm() {
                return config.getValue(hashSpecPrefix + ".algorithm", null);
            }
        };
    }

    private byte[] charsToBytes(char[] chars, String charSetName) {
        Charset charset = Charset.forName(charSetName);
        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }

    private char[] bytesToChars(byte[] bytes, int offset, int length, String charSetName, int totalChars) {
        Charset charset = Charset.forName(charSetName);
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(bytes, offset, length));
        if (totalChars > charBuffer.limit()) {
            totalChars = charBuffer.limit();
        }
        return Arrays.copyOf(charBuffer.array(), totalChars);
    }

    @Override
    public boolean keyExists(String keyPath) {
        try {
            return keyProvider.resolveKey(new KeyRefImpl(keyPath)) != null;
        } catch (KeyProviderException exception) {
            return false;
        }
    }

    /**
     * Conceal a string by encrypting it note that strings are concealed and
     * revealed using the default key pair
     * 
     * @param unsecureChars
     * @return
     */
    @Override
    public ConcealedString concealString(char[] unsecureChars, boolean internal) throws CypherException {
        ensureState(CypherServiceState.INITIALIZING, CypherServiceState.INITIALIZED);
        if (unsecureChars == null || unsecureChars.length == 0
                || unsecureChars.length > CONCEALEDSTRING_MAXIMUM_LENGTH) {
            throw new IllegalArgumentException("unsecureChars");
        }
        byte[] unsecure = null;
        byte[] unsecureWithLength = null;
        try {
            int length = unsecureChars.length;
            char[] extendedUnsecureChars;
            String salt = generateSalt(CONCEALEDSTRING_SALT_LENGTH);
            int saltLength = salt.length();
            extendedUnsecureChars = new char[length + saltLength];
            System.arraycopy(unsecureChars, 0, extendedUnsecureChars, 0, length);
            System.arraycopy(salt.toCharArray(), 0, extendedUnsecureChars, length, saltLength);

            unsecure = charsToBytes(extendedUnsecureChars, CONCEALEDSTRING_DEFAULT_CHARSET);

            unsecureWithLength = new byte[unsecure.length + 2];

            unsecureWithLength[0] = (byte) ((length & 0xFF00) >> 8);
            unsecureWithLength[1] = (byte) (length & 0xFF);
            System.arraycopy(unsecure, 0, unsecureWithLength, 2, unsecure.length);
            Cypher cypher;
            if (internal) {
                cypher = cypherProvider.getCypher(internalConcealCypher, internalConcealKeyRef);
            } else {
                cypher = cypherProvider.getCypher(externalConcealCypher, externalConcealKeyRef);
            }
            byte[] secureData = cypher.encrypt(unsecureWithLength);

            return new ConcealedStringImpl(secureData, internal);
        } finally {
            if (unsecure != null) {
                Arrays.fill(unsecure, (byte) 0);
            }
            if (unsecureWithLength != null) {
                Arrays.fill(unsecureWithLength, (byte) 0);
            }
        }
    }

    @Override
    public ConcealedString concealString(char[] unsecureChars) throws CypherException {
        return concealString(unsecureChars, false);
    }

    /**
     * Reveals a concealed string note that strings are concealed and revealed using
     * the default key pair
     * 
     * @param ConcealedString the concealed string to be revealed
     * @return the revealed string
     */
    private char[] revealString(ConcealedString concealedString) throws KeyException, CypherException {
        ensureState(CypherServiceState.INITIALIZING, CypherServiceState.INITIALIZED);
        if (concealedString instanceof ConcealedStringImpl) {
            ConcealedStringImpl concealedStringImpl = (ConcealedStringImpl) concealedString;
            if (!concealedStringImpl.isDestroyed()) {
                Cypher cypher;
                if (concealedStringImpl.isInternal()) {
                    cypher = cypherProvider.getCypher(internalConcealCypher, internalConcealKeyRef);
                } else {
                    cypher = cypherProvider.getCypher(externalConcealCypher, externalConcealKeyRef);
                }
                byte[] secureData = concealedStringImpl.getData();
                byte[] unsecureData = null;
                try {

                    unsecureData = cypher.decrypt(secureData);

                    int length = unsecureData[0] << 8 | unsecureData[1]; // length of the original string
                    if (length < 0 || length > CONCEALEDSTRING_MAXIMUM_LENGTH) {
                        throw new IllegalArgumentException("Not a valid ConcealedString");
                    }
                    return bytesToChars(unsecureData, 2, unsecureData.length - 2, CONCEALEDSTRING_DEFAULT_CHARSET,
                            length);
                } finally {
                    if (unsecureData != null) {
                        Arrays.fill(unsecureData, (byte) 0);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Not a valid ConcealedString");
    }

    public boolean validateInternalEncryption() throws Exception {
        String testString = "123456";

        StringCypher cypher = getStringCypher("internalConcealString");

        String secure = cypher.encrypt(testString);

        String unsecure = cypher.decrypt(secure);

        return testString.equals(unsecure);
    }

    public boolean validateConcealStringAlgorithm() throws KeyException, CypherException {
        char[] testString = new char[] { '1', '2', '3', '4', '5', '6' };
        ConcealedString concealedString = concealString(testString, true);
        char[] revealedString = revealString(concealedString);
        if (Arrays.equals(testString, revealedString)) {
            return true;
        } else {
            for (int i = 0; i < testString.length; i++) {
                String found;
                if (i <= revealedString.length) {
                    found = "'" + revealedString[i] + "'";
                } else {
                    found = "nothing";
                }
                System.out.println("[" + i + "] expected '" + testString[i] + "' but found " + found);
            }
            return false;
        }
    }

    @Override
    public void processWithKey(final String keyAlias, CypherAction<Key> action) throws CypherException {
        KeyVersion keyVersion = framedKeyProvider.resolveKey(new KeyRefImpl(keyAlias));
        Key key = keyVersion.getKey(Key.class);
        try {
            action.action(key);
        } catch (Exception exception) {
            throw new CypherException("processing with key failed", exception);
        }
    }

    @Override
    public void processWithPublicKey(final String keyAlias, CypherAction<PublicKey> action) throws CypherException {
        KeyVersion keyVersion = framedKeyProvider.resolveKey(new KeyRefImpl(keyAlias));
        PublicKey key = keyVersion.getKey(PublicKey.class);
        try {
            action.action(key);
        } catch (Exception exception) {
            throw new CypherException("processing with public key failed", exception);
        }
    }

    @Override
    public void processWithPrivateKey(final String keyAlias, CypherAction<PrivateKey> action) throws CypherException {
        KeyVersion keyVersion = framedKeyProvider.resolveKey(new KeyRefImpl(keyAlias));
        PrivateKey key = keyVersion.getKey(PrivateKey.class);
        try {
            action.action(key);
        } catch (Exception exception) {
            throw new CypherException("processing with private key failed", exception);
        }
    }

    @Override
    public void processWithConcealedString(final ConcealedString concealedString, CypherAction<char[]> action)
            throws CypherException {
        char[] unsecuredString = null;
        try {
            unsecuredString = revealString(concealedString);
            action.action(unsecuredString);
        } catch (Exception exception) {
            throw new CypherException("processing with concealed string failed", exception);
        } finally {
            if (unsecuredString != null) {
                Arrays.fill(unsecuredString, (char) 0);
            }
        }
    }

    @Override
    public <T> T processWithConcealedString(final ConcealedString concealedString, Class<T> type,
            CypherFunc<char[]> action) throws CypherException {
        char[] unsecuredString = null;
        try {
            unsecuredString = revealString(concealedString);
            return type.cast(action.action(unsecuredString));
        } catch (Exception exception) {
            throw new CypherException("processing with concealed string failed", exception);
        } finally {
            if (unsecuredString != null) {
                Arrays.fill(unsecuredString, (char) 0);
            }
        }
    }

    /**
     * generate salt from
     * https://dev.to/awwsmm/how-to-encrypt-a-password-in-java-42dh
     * 
     * @param length The length of the salt to generate (>= 10)
     * @return A salt of the specified length
     */
    @Override
    public String generateSalt(final int length) {
        if (length < MINIMUM_SALT_LENGTH) {
            throw new IllegalArgumentException("length must be > " + MINIMUM_SALT_LENGTH);
        }

        byte[] salt = new byte[length];
        this.secureRandom.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    @Override
    public Cypher getCypher(final String cypherName) throws CypherException {
        return cypherProvider.getCypher(getCypherSpec(cypherName), getKeyRef(cypherName));
    }

    @Override
    public StringCypher getStringCypher(final String cypherName) throws CypherException {
        return cypherProvider.getStringCypher(getCypherSpec(cypherName), getKeyRef(cypherName));
    }

    @Override
    public Endorser getEndorser(final String cypherName) throws CypherException {
        return new EndorserImpl(this, getCypherSpec(cypherName), getKeyRef(cypherName));
    }

    @Override
    public HashCypher getHashCypher(String cypherName) throws CypherException {
        return new HashCypherImpl(getHashSpec(cypherName), null);
    }

    @Override
    public HashCypher getHashCypher(String cypherName, byte[] salt) throws CypherException {
        return new HashCypherImpl(getHashSpec(cypherName), salt);
    }

    @Override
    public void close() throws Exception {
        stateLock.lock();
        try {
            this.state = CypherServiceState.COMPROMISED;
            if (this.framedKeyProvider != null) {
                this.framedKeyProvider.close();
            }
            this.state = CypherServiceState.CLOSED;
        } finally {
            stateLock.lock();
        }
    }
}