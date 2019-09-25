package org.daisleyharrison.security.services.key;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.common.models.key.KeyProvider.KeyVersion;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class KeyServiceTest {

    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    private ConfigurationServiceProvider configurationService;

    private KeyServiceProvider keyService;

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

    /**
     * Rigorous Test :-)
     */
    @Test
    public void initailizeConfigurationService() throws Exception {
        File configFile = new File(locatePath(".\\key-service\\src\\test\\resources\\testConfig.yaml", "key-service"));
        System.out.println("using config file: " + configFile.getAbsolutePath());

        configurationService = _serviceProvider.provideService(ConfigurationServiceProvider.class);
        if (!configurationService.isInitialized()) {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                try (Stage stage = configurationService.beginInitialize()) {
                    configurationService.setSource(inputStream);
                    configurationService.configure();
                }
            }
        }
        assertTrue(true);
    }

    @Test
    public void initailizeKeyService() throws Exception {
        initailizeConfigurationService();
        keyService = _serviceProvider.provideService(KeyServiceProvider.class);
        if (!keyService.isInitialized()) {
            try (Stage stage = keyService.beginInitialize()) {
                keyService.configure();
            }
        }
        assertTrue(true);
    }

    @Test
    public void initailizeAndCloseKeyService() throws Exception {
        initailizeConfigurationService();
        initailizeKeyService();
        keyService.close();
        assertTrue(true);
    }

    @Test
    public void resolveKeyFromTest1() throws Exception {
        initailizeConfigurationService();
        initailizeKeyService();

        KeyVersion keyVersion = keyService.resolveKey(new KeyReference() {

            @Override
            public String getPath() {
                return "test1/jwe-bearer";
            }
        });
        assertNotNull(keyVersion);
    }

    @Test
    public void resolveKeyFromTest2() throws Exception {
        initailizeConfigurationService();
        initailizeKeyService();

        KeyVersion keyVersion = keyService.resolveKey(new KeyReference() {

            @Override
            public String getPath() {
                return "test2/jwe-bearer";
            }
        });
        assertNotNull(keyVersion);
    }

    @Test
    public void resolvePublicKeyFromTest1() throws Exception {
        initailizeConfigurationService();
        initailizeKeyService();

        KeyVersion keyVersion = keyService.resolveKey(new KeyReference() {

            @Override
            public String getPath() {
                return "test2/concealedstring/public";
            }
        });
        assertTrue(PublicKey.class.isInstance(keyVersion.getKey()));
    }

    @Test
    public void resolvePrivateKeyFromTest1() throws Exception {
        initailizeConfigurationService();
        initailizeKeyService();

        KeyVersion keyVersion = keyService.resolveKey(new KeyReference() {

            @Override
            public String getPath() {
                return "test2/concealedstring";
            }
        });
        assertTrue(PrivateKey.class.isInstance(keyVersion.getKey()));
    }
}
