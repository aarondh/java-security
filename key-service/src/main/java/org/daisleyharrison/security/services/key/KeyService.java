package org.daisleyharrison.security.services.key;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.ServiceNotFoundException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.key.CachedKeyProvider;
import org.daisleyharrison.security.common.models.key.KeyProvider;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.utilities.StageImpl;
import org.daisleyharrison.security.services.key.internal.CachedKeyProviderImpl;
import org.daisleyharrison.security.services.key.internal.KeyStoreKeyProviderImpl;
import org.daisleyharrison.security.services.key.internal.MultiplexedKeyProviderImpl;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class KeyService implements KeyServiceProvider {
    private static final String CONFIG_ROOT = "key-service";
    private static final String CONFIG_KEY_STORES = CONFIG_ROOT + ".keystores";
    private static Logger LOGGER = LoggerFactory.getLogger(KeyService.class);
    private static final int DEFAULT_CACHED_KEY_TTL = 10 * 60; // in seconds
    private ReentrantLock stateLock;

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

    private State state;

    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    private CachedKeyProvider keyProvider;

    private static KeyServiceProvider s_instance;

    public static KeyServiceProvider getInstance() {
        if (s_instance == null) {
            s_instance = new KeyService();
        }
        return s_instance;
    }

    protected KeyService() {
        this.stateLock = new ReentrantLock();
        this.state = State.CREATED;
    }

    @Override
    public boolean isInitialized() {
        this.stateLock.lock();
        try {
            return this.state != State.CREATED && this.state != State.CLOSED && this.state != State.INITIALIZING;
        } finally {
            this.stateLock.unlock();
        }
    }

    @Override
    public boolean isReady() {
        this.stateLock.lock();
        try {
            return this.state == State.INITIALIZED;
        } finally {
            this.stateLock.unlock();
        }
    }

    private void assertInitializing() {
        this.stateLock.lock();
        try {
            if (this.state != State.INITIALIZING) {
                throw new IllegalStateException(
                        "key-service is in state " + state.toString() + " and is not initializing.");
            }
        } finally {
            this.stateLock.unlock();
        }
    }

    private void assertReady() {
        this.stateLock.lock();
        try {
            if (!isReady()) {
                throw new IllegalStateException("key-service is in state " + state.toString() + " and is not ready.");
            }
        } finally {
            this.stateLock.unlock();
        }
    }

    @Override
    public Stage beginInitialize() {
        this.stateLock.lock();
        if (this.state != State.CREATED && this.state != State.CLOSED) {
            this.stateLock.unlock();
            throw new IllegalStateException(
                    "key-service is in state " + state.toString() + " and cannot be initialized.");
        }
        this.state = State.INITIALIZING;
        LOGGER.info("key-service initializing");
        return new StageImpl(() -> {
            if (this.state != State.INITIALIZING) {
                this.stateLock.unlock();
                throw new IllegalStateException(
                        "key-service is in state " + state.toString() + " and was not initialing.");
            }
            if (this.keyProvider == null) {
                this.state = State.COMPROMIZED;
                LOGGER.info("key-service was not correctly configured.  State set to {}", state.toString());
            } else {
                this.state = State.INITIALIZED;
                LOGGER.info("key-service initialized");
            }
            this.stateLock.unlock();
        });
    }

    @Override
    public void configure() throws Exception {
        assertInitializing();
        LOGGER.info("key-service configuring");
        try {
            ConfigurationServiceProvider config = _serviceProvider.provideService(ConfigurationServiceProvider.class);
            Set<String> keyStoreNames = config.getNames(CONFIG_KEY_STORES);
            MultiplexedKeyProviderImpl multiKeyProvider = new MultiplexedKeyProviderImpl();
            int minCachedKeyTTL = Integer.MAX_VALUE;
            for (String keyStoreName : keyStoreNames) {
                LOGGER.info("key-service configuring keyStore {}", keyStoreName);
                String keyStoreConfig = CONFIG_KEY_STORES + "." + keyStoreName;
                Path keyStorePath = Path.of(config.getValue(keyStoreConfig + ".path"));
                char[] keyStorePassword = config.getCharsValue(keyStoreConfig + ".password");
                String keyPathRoot = config.getValue(keyStoreConfig + ".keyPath");
                String keyStoreType = config.getValue(keyStoreConfig + ".type", "JKS");
                int cachedKeyTTL = config.getIntegerValue(keyStoreConfig + ".cachedKeyTTL", DEFAULT_CACHED_KEY_TTL);
                if (cachedKeyTTL > minCachedKeyTTL) {
                    minCachedKeyTTL = cachedKeyTTL;
                }
                KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, keyStorePassword);
                KeyProvider kskProvider = new KeyStoreKeyProviderImpl(keyPathRoot, keyStore, keyStorePassword);
                multiKeyProvider.addKeyProvider(kskProvider);
                LOGGER.info("key-service keyStore {} configured for key paths starting with {}", keyStoreName,
                        keyPathRoot);
            }
            KeyProvider keyProvider;
            switch (multiKeyProvider.size()) {
            case 0:
                throw new KeyProviderException("No Key Stores configured");
            case 1:
                keyProvider = multiKeyProvider.iterator().next();
                break;
            default:
                keyProvider = multiKeyProvider;
                break;
            }
            this.keyProvider = new CachedKeyProviderImpl(keyProvider, Duration.ofSeconds(minCachedKeyTTL));

        } catch (ServiceNotFoundException exception) {
            this.state = State.COMPROMIZED;
            throw new IllegalStateException(exception);
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
        LOGGER.info("key-service opening {} keyStore {}", keyStoreType, path);
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(inputStream, password);
            return keyStore;
        }
    }

    @Override
    public boolean isSupported(KeyReference keyRef) {
        return this.keyProvider.isSupported(keyRef);
    }

    @Override
    public KeyVersion resolveKey(KeyReference keyRef) throws KeyProviderException {
        assertReady();
        return this.keyProvider.resolveKey(keyRef);
    }

    @Override
    public void evict(String path) {
        assertReady();
        this.keyProvider.evict(path);
    }

    @Override
    public void clear() {
        assertReady();
        this.keyProvider.clear();
    }

    @Override
    public void close() throws Exception {
        if (this.keyProvider != null) {
            this.keyProvider.close();
        }
        this.state = State.CLOSED;
    }

}