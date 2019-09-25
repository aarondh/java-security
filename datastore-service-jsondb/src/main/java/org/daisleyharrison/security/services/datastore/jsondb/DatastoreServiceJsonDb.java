package org.daisleyharrison.security.services.datastore.jsondb;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.utilities.StageImpl;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.services.datastore.jsondb.models.DatastoreCollectionImpl;
import org.daisleyharrison.security.services.datastore.jsondb.models.JsonDbCypherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsondb.JsonDBTemplate;
import io.jsondb.crypto.ICipher;

public class DatastoreServiceJsonDb implements DatastoreServiceProvider {
    private static Logger LOGGER = LoggerFactory.getLogger(DatastoreServiceJsonDb.class);
    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    public enum State {
        CREATED, INITIALIZING, INITIALIZED, CLOSED, COMPROMISED, ERROR
    }

    private State state;
    private JsonDBTemplate jsonDbTemplate;

    private ConfigurationServiceProvider getConfig() throws ServiceNotFoundException {
        return _serviceProvider.provideService(ConfigurationServiceProvider.class);
    }

    private String getBackupPath() {
        try {
            return getConfig().getValue("datastore.backup-path", null);
        } catch (ServiceNotFoundException exception) {
            return null;
        }
    }

    @Override
    public boolean isReady() {
        return state == State.INITIALIZED;
    }

    @Override
    public boolean isInitialized() {
        return state == State.INITIALIZED;
    }

    private void assertReady() {
        if (!isReady()) {
            throw new IllegalStateException(
                    "datastore-service is in state " + this.state.toString() + " and is not ready");
        }
    }

    private void assertInitializing() {
        if (state != State.INITIALIZING) {
            throw new IllegalStateException(
                    "datastore-service is in state " + this.state.toString() + " and is not initializing");
        }
    }

    private static final DatastoreServiceProvider INSTANCE = new DatastoreServiceJsonDb();

    public static DatastoreServiceProvider getInstance() {
        return INSTANCE;
    }

    protected DatastoreServiceJsonDb() {
        this.state = State.CREATED;
    }

    public JsonDBTemplate getJsonDbTemplate() {
        return this.jsonDbTemplate;
    }

    @Override
    public Stage beginInitialize() {
        if (this.state != State.CREATED && this.state != State.CLOSED) {
            throw new IllegalStateException("datastore-service cannot initalize in state " + this.state.toString());
        }
        this.state = State.INITIALIZING;
        LOGGER.info("datastore-service initializing");
        return new StageImpl(() -> {
            if (this.state != State.INITIALIZING) {
                throw new IllegalStateException("datastore-service is initalizing in state " + this.state.toString());
            }
            if (this.jsonDbTemplate == null) {
                this.state = State.ERROR;
                throw new IllegalStateException("datastore-service was not configured correctly");
            }
            this.state = State.INITIALIZED;
            LOGGER.info("datastore-service initialized");
        });
    }

    private StringCypher cypher;

    @Override
    public void setCypher(StringCypher cypher) {
        assertInitializing();
        this.cypher = cypher;
    }

    @Override
    public void configure() {
        assertInitializing();
        try {
            ConfigurationServiceProvider config = getConfig();
            String fileLocation = config.getValue("datastore.file-location");
            LOGGER.info("datastore-service datastore.file-location: {}", fileLocation);

            String baseScanPackage = config.getValue("datastore.base-scan-package");
            LOGGER.info("datastore-service datastore.base-scan-package: {}", baseScanPackage);

            if (this.cypher == null) {
                this.jsonDbTemplate = new JsonDBTemplate(fileLocation, baseScanPackage);
            } else {
                ICipher jsonDbCypher = new JsonDbCypherImpl(this.cypher);
                this.jsonDbTemplate = new JsonDBTemplate(fileLocation, baseScanPackage, jsonDbCypher);
            }

        } catch (ServiceNotFoundException exception) {
            throw new IllegalStateException("datastore-service could not find the configuration-service.");
        }
    }

    @Override
    public <T> DatastoreCollection<T> openCollection(Class<T> type, String idFieldName) {
        assertReady();
        return new DatastoreCollectionImpl<T>(this, type, idFieldName);
    }

    @Override
    public void close() {
        if (getBackupPath() != null) {
            this.jsonDbTemplate.backup(getBackupPath());
        }
        this.cypher = null;
        this.jsonDbTemplate = null;
        this.state = State.CLOSED;
    }
}