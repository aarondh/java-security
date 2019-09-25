package org.daisleyharrison.security.services.openId;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.openId.OpenIdServiceConfig;
import org.daisleyharrison.security.common.models.openId.OpenIdState;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.services.configuration.ConfigurationService;
import org.daisleyharrison.security.common.spi.OpenIdServiceProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestOpenIdService {

    private OpenIdServiceProvider openidService;
    private ConfigurationServiceProvider configurationService;

    @Before
    public void setUp() throws Exception {
        // start the service
        openidService = new OpenIdService();
        configurationService = ConfigurationService.getInstance();
        File configFile = new File(locatePath(".\\openid-service\\clientConfig.yaml", "openid-service"));
        System.out.println("using config file: " + configFile.getAbsolutePath());
        try (InputStream inputStream = new FileInputStream(configFile)) {
            try (Stage stage = configurationService.beginInitialize()) {
                configurationService.setSource(inputStream);
                configurationService.configure();
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        dumpCurrentDirectory();
        File configServiceFile = new File(locatePath(".\\openid-service\\openid.json", "openid-service"));
        System.out.println("using service config file: " + configServiceFile.getAbsolutePath());
        OpenIdServiceConfig config = mapper.readValue(configServiceFile, OpenIdServiceConfig.class);

        String clientId = configurationService.getValue("google.client_id", null);
        char[] clientSecret = configurationService.getCharsValue("google.client_secret", null);

        try (Stage stage = openidService.beginInitialize()) {

            openidService.setConfiguration(config);

            openidService.configure();

            openidService.setDomain("https://localhost:3000");

            openidService.setDebug(true);

            openidService.setClientCredentials("google.com", clientId, clientSecret);

        }

    }

    private String locatePath(String path, String component) {
        if (currentDirectory().contains(component)) {
            return "." + path;
        } else {
            return path;
        }
    }

    private String currentDirectory() {
        return new File(".").getAbsolutePath();
    }

    private void dumpCurrentDirectory() {
        System.out.println("Current working directory : " + currentDirectory());
    }

    @After
    public void tearDown() throws Exception {
        openidService.close();
        configurationService.close();
    }

    /**
     * test retrieval of salt generation
     */
    @Test
    public void testDiscovery() throws Exception {
        boolean result1 = openidService.discover("google.com").build().join();

        assertTrue("google.com openid discovery document read", result1);

        boolean result2 = openidService.discover("microsoft.com").build().join();

        assertTrue("microsoft.com openid discovery document read", result2);
    }

    /**
     * test retrieval of authorization URI
     */
    @Test
    public void testGetAuthenticationURI() throws Exception {
        String redirectUri = configurationService.getValue("google.redirect_uri", null);

        URI authenticationUri = openidService.getAuthenticationURI("google.com").setState("Hi there")
                .setProperty("redirect_uri", redirectUri).build().join();

        System.out.printf("authenticationUri: %s\n", authenticationUri);
    }

    /**
     * test state token encryption/decryption
     */
    @Test
    public void testStateToken() throws Exception {
        String redirectUri = configurationService.getValue("google.redirect_uri", null);

        String stateToken = openidService.createStateToken("google.com").setState("Hi there")
                .setProperty("redirect_uri", redirectUri).build().join();

        OpenIdState openidState = openidService.validateStateToken(stateToken).join();
        assertEquals("Hi there", openidState.getState());
        assertEquals("google.com", openidState.getIssuer());
    }

    /**
     * test bearer token encryption/decryption
     */
    @Test
    public void testBearerToken() throws Exception {
        String redirectUri = configurationService.getValue("google.redirect_uri", null);

        String bearerToken = openidService.createBearerToken(2, 10).setProperty("redirect_uri", redirectUri).build()
                .join();

        AuthClaims claims = openidService.validateBearerToken(bearerToken).join();
        assertEquals("TOI::BEARER", claims.getSubject());
        assertEquals(openidService.getDomain(), claims.getIssuer());
    }

}
