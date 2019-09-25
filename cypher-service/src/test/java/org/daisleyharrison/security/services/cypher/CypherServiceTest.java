package org.daisleyharrison.security.services.cypher;

import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.CypherServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.CypherEncryptionImpl;
import org.daisleyharrison.security.common.models.Endorser;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.cypher.ConcealedString;
import org.daisleyharrison.security.common.models.cypher.Cypher;
import org.daisleyharrison.security.common.models.cypher.HashCypher;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.models.key.KeyFrame;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class CypherServiceTest {

    private static final LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    private CypherServiceProvider cypherService;

    private KeyServiceProvider keyService;

    private ConfigurationServiceProvider configurationService;

    @Before
    public void setUp() throws Exception {
        // start the service
        configurationService = _serviceProvider.provideService(ConfigurationServiceProvider.class);
        keyService = _serviceProvider.provideService(KeyServiceProvider.class);
        cypherService = _serviceProvider.provideService(CypherServiceProvider.class);
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

    @After
    public void tearDown() throws Exception {
        /*
         * if (configurationService != null) { configurationService.close(); } if
         * (keyService != null) { keyService.close(); } if (cypherService != null) {
         * cypherService.close(); }
         */
    }

    @Test
    public void initializeConfigurationService() throws Exception {
        if (!configurationService.isInitialized()) {
            File configFile = new File(
                    locatePath("./cypher-service/src/test/resources/testconfig.yaml", "cypher-service"));
            System.out.println("using config file: " + configFile.getAbsolutePath());
            try (InputStream inputStream = new FileInputStream(configFile)) {
                try (Stage stage = configurationService.beginInitialize()) {
                    configurationService.setSource(inputStream);
                    configurationService.configure();
                }
            }
        }
    }

    @Test
    public void initializeKeyService() throws Exception {
        initializeConfigurationService();
        if (!keyService.isInitialized()) {
            try (Stage stage = keyService.beginInitialize()) {
                keyService.configure();
            }
        }
    }

    @Test
    public void initializeCypherService() throws Exception {
        initializeKeyService();
        if (!cypherService.isInitialized()) {
            try (Stage stage2 = cypherService.beginInitialize()) {
                cypherService.configure();
            }
        }
    }

    /**
     * test retrieval of salt generation
     */
    @Test
    public void testSalt() {
        String salt = cypherService.generateSalt(128);
        System.out.println("salt: " + salt);
        assertTrue("generated salt was not the expected length", salt.length() >= 128);
    }

    /**
     * test retrieval of password hashing
     */
    @Test
    public void testPasswordHashVerified() throws Exception {
        byte[] salt = cypherService.generateSalt(128).getBytes();
        char[] password = "thelazydog-1234".toCharArray();
        HashCypher hashCypher = cypherService.getHashCypher("passwordHash", salt);
        String hashedPassword = hashCypher.hash(password);
        boolean verified = hashCypher.verify(hashedPassword, password);
        assertTrue("password not verified", verified);
    }

    /**
     * test retrieval of password hashing
     */
    @Test
    public void testPasswordHashNotVerified() throws Exception {
        byte[] salt = cypherService.generateSalt(128).getBytes();
        char[] password1 = "thelazydog-1234".toCharArray();
        char[] password2 = "thelazydog$1234".toCharArray();
        HashCypher hashCypher = cypherService.getHashCypher("passwordHash", salt);
        String hashedPassword = hashCypher.hash(password1);
        boolean verified = hashCypher.verify(hashedPassword, password2);
        assertFalse("passwordshould not have been verified", verified);
    }

    private static final String QUICK_TEST_STRING = "The quick brown fox jumped over the lazy dog (!@#$%^&*-_=+[{]};:\\|'\"<,>.?/]) (0123456789)";
    private static final char[] QUICK_TEST_CHAR_ARRAY = QUICK_TEST_STRING.toCharArray();

    /**
     * test ConcealString
     */
    @Test
    public void concealString() throws Exception {
        initializeCypherService();
        ConcealedString concealedString = cypherService.concealString(QUICK_TEST_CHAR_ARRAY);
    }

    /**
     * test ConcealString
     */
    @Test
    public void processWithConcealedString() throws Exception {
        initializeCypherService();
        ConcealedString concealedString = cypherService.concealString(QUICK_TEST_CHAR_ARRAY);
        boolean result = cypherService.processWithConcealedString(concealedString, Boolean.class, (revealedString) -> {
            return Arrays.equals(QUICK_TEST_CHAR_ARRAY, revealedString);
        });
        assertTrue("Consealed string failed to work correctly", result);
    }

    @Test
    public void testInternalConcealedString() throws Exception {
        initializeCypherService();
        ConcealedString concealedString = cypherService.concealString(QUICK_TEST_CHAR_ARRAY, true);
    }

    @Test
    public void testProcessWithInternalConcealedString() throws Exception {
        initializeCypherService();
        ConcealedString concealedString = cypherService.concealString(QUICK_TEST_CHAR_ARRAY, true);
        boolean result = cypherService.processWithConcealedString(concealedString, Boolean.class, (revealedString) -> {
            return Arrays.equals(QUICK_TEST_CHAR_ARRAY, revealedString);
        });
        assertTrue("Consealed string failed to work correctly", result);
    }

    /**
     * test encrypt/decript
     */
    @Test
    public void encrypt_decrypt() throws Exception {

        Charset charset = Charset.forName("UTF8");

        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(QUICK_TEST_CHAR_ARRAY));

        byte[] unsecureData = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());

        initializeCypherService();
        try (KeyFrame frame = cypherService.openFrame()) {
            Cypher cypher = cypherService.getCypher("semetric");
            byte[] secureData = cypher.encrypt(unsecureData);
            byte[] decryptedData = cypher.decrypt(secureData);
            assertTrue("decrypted data does not match", Arrays.equals(unsecureData, decryptedData));
        }
    }

    @Test
    public void encrypt_decrypt_string() throws Exception {

        initializeCypherService();
        StringCypher cypher = cypherService.getStringCypher("semetric");
        String secureString = cypher.encrypt("The quick brown fox jumped over the lazy dog");
        String decryptedString = cypher.decrypt(secureString);
        assertEquals("The quick brown fox jumped over the lazy dog", decryptedString);
    }

    @Test
    public void cypherTest() throws Exception {
        char[] unsecureChars = QUICK_TEST_CHAR_ARRAY;

        Charset charset = Charset.forName("UTF8");

        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(unsecureChars));

        byte[] unsecureData = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());

        initializeCypherService();

        Cypher cypher = cypherService.getCypher("semetric");

        byte[] secureData = cypher.encrypt(unsecureData, null, null).getSecureData();

        byte[] decryptedData = cypher.decrypt(new CypherEncryptionImpl(secureData), null);

        assertTrue("decrypted data does not match", Arrays.equals(unsecureData, decryptedData));
    }

    @Test
    public void stringCypherTest() throws Exception {
        String unsecureString = QUICK_TEST_STRING;

        initializeCypherService();

        StringCypher stringCypher = cypherService.getStringCypher("semetric");

        String secureString = stringCypher.encrypt(unsecureString);

        String decryptedString = stringCypher.decrypt(secureString);

        assertTrue("decrypted data does not match", unsecureString.equals(decryptedString));
    }

    @Test
    public void endorserTest() throws Exception {
        String unverifiedString = QUICK_TEST_STRING;

        initializeCypherService();

        Endorser endorser = cypherService.getEndorser("endorser");

        String signature = endorser.sign(unverifiedString);

        boolean verified = endorser.verify(signature, unverifiedString);

        assertTrue("signature not verified", verified);
    }

    @Test
    public void hashingTest() throws Exception {
        initializeCypherService();

        char[] password = "One2-BuckleMySh0e".toCharArray();

        HashCypher hashCypher = _serviceProvider.provideService(CypherServiceProvider.class).getHashCypher("passwordHash");
        String hashedPassword = hashCypher.hash(password);
        boolean verified = hashCypher.verify(hashedPassword, password);

        assertTrue("password match", verified);
    }
}