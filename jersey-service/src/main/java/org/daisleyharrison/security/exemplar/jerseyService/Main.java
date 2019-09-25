package org.daisleyharrison.security.samples.jerseyService;

import org.daisleyharrison.security.common.exceptions.OpenIdException;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.models.openId.OpenIdServiceConfig;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.CypherServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.spi.OpenIdServiceProvider;
import org.daisleyharrison.security.common.spi.ProfileServiceProvider;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.samples.jerseyService.config.*;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.util.Set;

import javax.management.ServiceNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class.
 *
 */
public class Main {
    public static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    // Default URI the Grizzly HTTP server will listen on
    public static final String DEFAULT_BASE_URI = "http://localhost:3000/c2id/";
    public static final String DEFAULT_HOME_URI = "/c2id/home";

    public static URI getBaseUri() {
        try {
            return new URI(getConfigurationService().getValue("server.base_uri", DEFAULT_BASE_URI));
        } catch (URISyntaxException | ServiceNotFoundException exception) {
            try {
                return new URI(DEFAULT_BASE_URI);
            } catch (URISyntaxException exception2) {
                return null;
            }
        }
    }

    public static URI getHomeUri() {
        try {
            return new URI(getConfigurationService().getValue("server.home_uri", DEFAULT_HOME_URI));
        } catch (URISyntaxException | ServiceNotFoundException exception) {
            try {
                return new URI(DEFAULT_HOME_URI);
            } catch (URISyntaxException exception2) {
                return null;
            }
        }
    }

    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates Grizzly HTTP server exposing JAX-RS resources defined in this
     * application.
     * 
     * @return Grizzly HTTP server.
     */
    public static HttpServer createServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example.rest package
        final ResourceConfig rc = new AppResourceConfig();
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(getBaseUri(), rc);
        try {
            ServerConfiguration serverConfiguration = httpServer.getServerConfiguration();
            ConfigurationServiceProvider config = getConfigurationService();
            getDatastoreService(); // ensure the datastore is initialized
            Set<String> statics = config.getNames("server.statics");
            statics.forEach((path) -> {
                String pathEntry = "server.statics.'" + path + "'";
                boolean isFileCacheEnabled = config.getBooleanValue(pathEntry + ".cache", true);
                String packageName = config.getValue(pathEntry + ".package", null);
                if (packageName == null) {
                    packageName = config.getValue(pathEntry, null);
                }
                if (packageName != null) {
                    CLStaticHttpHandler clStaticHttpHandler = new CLStaticHttpHandler(Main.class.getClassLoader(),
                            packageName);
                    clStaticHttpHandler.setFileCacheEnabled(isFileCacheEnabled);
                    serverConfiguration.addHttpHandler(clStaticHttpHandler, path);
                    LOGGER.info("mapping \"{}\" to package {} with {}", path, packageName,
                            (isFileCacheEnabled ? "caching" : "no cache"));
                }
            });
        } catch (ServiceNotFoundException exception) {

        }
        return httpServer;

    }

    private static String locatePath(String path, String component) {
        if (currentDirectory().contains(component)) {
            return "." + path;
        } else {
            return path;
        }
    }

    private static String currentDirectory() {
        return new File(".").getAbsolutePath();
    }

    public static ConfigurationServiceProvider getConfigurationService() throws ServiceNotFoundException {
        ConfigurationServiceProvider configurationService = _serviceProvider.provideService(ConfigurationServiceProvider.class);
        // ConfigurationServiceProvider configurationService = new
        // ConfigurationServiceX();

        if (!configurationService.isInitialized()) {
            try {
                File configFile = new File(locatePath("./jersey-service/config.yaml", "jersey-service"));
                LOGGER.info("configuration-service using configuration file: " + configFile.getAbsolutePath());
                try (InputStream yamlStream = new FileInputStream(configFile)) {
                    try (Stage stage = configurationService.beginInitialize()) {
                        configurationService.setSource(yamlStream);
                        configurationService.configure();
                    } catch (SignatureException exception) {
                        LOGGER.error("configuration-service configuration file has an invalid signature.", exception);
                        throw new ServiceNotFoundException(
                                "configuration-service configuration file has an invalid signature.");
                    } catch (Exception exception) {
                        LOGGER.error("configiration-service failed to initialize", exception);
                        throw new ServiceNotFoundException("configiration-service failed to initialize");
                    }
                }
            } catch (FileNotFoundException exception) {
                LOGGER.error("configuration-service configuration file not found.", exception);
            } catch (IOException exception) {
                LOGGER.error("configuration-service configuration file failed.", exception);
            }
        }
        return configurationService;
    }

    public static CypherServiceProvider getCypherService() throws ServiceNotFoundException {
        getKeyService();
        CypherServiceProvider service = _serviceProvider.provideService(CypherServiceProvider.class);
        if (!service.isInitialized()) {
            try (Stage stage = service.beginInitialize()) {
                service.configure();
            } catch (Exception exception) {
                LOGGER.error("cypher-service configuration failed.", exception);
            }
        }

        return service;
    }

    public static KeyServiceProvider getKeyService() throws ServiceNotFoundException {
        KeyServiceProvider service =  _serviceProvider.provideService(KeyServiceProvider.class);
        if (!service.isInitialized()) {
            try (Stage stage = service.beginInitialize()) {
                service.configure();
            } catch (Exception exception) {
                LOGGER.error("key-service configuration failed.", exception);
            }
        }

        return service;
    }

    public static DatastoreServiceProvider getDatastoreService() throws ServiceNotFoundException {
        DatastoreServiceProvider service =  _serviceProvider.provideService(DatastoreServiceProvider.class);

        if (!service.isInitialized()) {
            try (Stage stage = service.beginInitialize()) {
                String dataStoreCypher = getConfigurationService().getValue("datastore.cypher", null);
                StringCypher cypher = getCypherService().getStringCypher(dataStoreCypher);
                service.setCypher(cypher);
                service.configure();
            } catch (Exception exception) {
                LOGGER.error("datastore-service configuration failed.", exception);
            }
        }

        return service;
    }

    public static OpenIdServiceProvider getOpenIdService() throws ServiceNotFoundException {
        OpenIdServiceProvider openidService = _serviceProvider.provideService(OpenIdServiceProvider.class);

        if (!openidService.isInitialized()) {
            try (Stage stage = openidService.beginInitialize()) {
                File openidServiceConfigFile = new File(".\\openid-service\\openid.json");
                LOGGER.info("openid-service using configuration file: " + openidServiceConfigFile.getAbsolutePath());
                OpenIdServiceConfig config = objectMapper.readValue(openidServiceConfigFile, OpenIdServiceConfig.class);
                openidService.setConfiguration(config);
                openidService.configure();
                openidService.setDebug(true);
                openidService.setDomain(getBaseUri().toString());

                String client_id1 = getConfigurationService().getValue("openid.'google.com'.client_id", null);
                char[] client_secret1 = getConfigurationService().getCharsValue("openid.'google.com'.client_secret",
                        null);
                openidService.setClientCredentials("google.com", client_id1, client_secret1);

                String client_id2 = getConfigurationService().getValue("openid.'microsoft.com'.client_id", null);
                char[] client_secret2 = getConfigurationService().getCharsValue("openid.'microsoft.com'.client_secret",
                        null);
                openidService.setClientCredentials("microsoft.com", client_id2, client_secret2);
            } catch (OpenIdException exception) {
                LOGGER.error("openid-service configuration failed.", exception);
            } catch (FileNotFoundException exception) {
                LOGGER.error("openid-service configuration file not found.", exception);
            } catch (Exception exception) {
                LOGGER.error("openid-service configuration file failed.", exception);
            }
        }
        return openidService;
    }

    public static TokenizerServiceProvider getTokenizerService() throws ServiceNotFoundException {
        TokenizerServiceProvider tokenizerService = _serviceProvider.provideService(TokenizerServiceProvider.class);

        if (!tokenizerService.isInitialized()) {
            try (Stage stage = tokenizerService.beginInitialize()) {
                tokenizerService.configure();
            } catch (Exception exception) {
                LOGGER.error("tokenizer-service configuration failed.", exception);
            }
        }

        return tokenizerService;
    }

    public static ProfileServiceProvider getProfileService() throws ServiceNotFoundException {
        ProfileServiceProvider service = _serviceProvider.provideService(ProfileServiceProvider.class);

        if (!service.isInitialized()) {
            try (Stage stage = service.beginInitialize()) {
                service.configure();
            } catch (Exception exception) {
                LOGGER.error("profile-service configuration failed.", exception);
            }
        }

        return service;
    }

    /**
     * Main method.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = createServer();
        System.out.println(String.format(
                "Jersey app started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...",
                getBaseUri()));
        System.in.read();
        server.shutdownNow();
    }
}
