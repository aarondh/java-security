package org.daisleyharrison.security.services.datastore.mongodb;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.utilities.StageImpl;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.services.datastore.mongodb.models.DatastoreCollectionImpl;

import org.jongo.Jongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class DatastoreServiceMongoDb implements DatastoreServiceProvider {
    private static Logger LOGGER = LoggerFactory.getLogger(DatastoreServiceMongoDb.class);
    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    public enum State {
        CREATED, INITIALIZING, INITIALIZED, CLOSED, COMPROMISED, ERROR
    }

    private State state;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private Jongo jongo;

    private ConfigurationServiceProvider getConfig() throws ServiceNotFoundException {
        return _serviceProvider.provideService(ConfigurationServiceProvider.class);
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

    private static final DatastoreServiceProvider INSTANCE = new DatastoreServiceMongoDb();

    public static DatastoreServiceProvider getInstance() {
        return INSTANCE;
    }

    protected DatastoreServiceMongoDb() {
        this.state = State.CREATED;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        return this.mongoDatabase;
    }

    public Jongo getJongo() {
        return this.jongo;
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
            if (this.mongoDatabase == null) {
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
            String mongoClientUri = config.getValue("datastore.mongo.clientUri");
            LOGGER.info("datastore-service datastore.mongo.clientUri: {}", mongoClientUri);

            String databaseName = config.getValue("datastore.mongo.database");
            LOGGER.info("datastore-service datastore.mongo.database: {}", databaseName);

            this.mongoClient = new MongoClient(new MongoClientURI(mongoClientUri));

            this.mongoDatabase = mongoClient.getDatabase(databaseName);

            this.jongo = new Jongo(mongoClient.getDB(databaseName));


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
        this.cypher = null;
        if(this.mongoClient!=null){
            this.mongoClient.close();
        }
        this.state = State.CLOSED;
    }
}