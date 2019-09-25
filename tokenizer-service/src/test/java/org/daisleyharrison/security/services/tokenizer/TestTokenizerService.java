package org.daisleyharrison.security.services.tokenizer;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.exceptions.NonceExpiredException;
import org.daisleyharrison.security.common.exceptions.NonceReplayException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;

public class TestTokenizerService {

    public static class TestData {
        private int intValue0;
        private int intValue1;
        private String strValue0;
        private String strValue1;
        private TestData nested;

        /**
         * @return int return the intValue0
         */
        public int getIntValue0() {
            return intValue0;
        }

        /**
         * @param intValue0 the intValue0 to set
         */
        public void setIntValue0(int intValue0) {
            this.intValue0 = intValue0;
        }

        /**
         * @return int return the intValue1
         */
        public int getIntValue1() {
            return intValue1;
        }

        /**
         * @param intValue1 the intValue1 to set
         */
        public void setIntValue1(int intValue1) {
            this.intValue1 = intValue1;
        }

        /**
         * @return String return the strValue0
         */
        public String getStrValue0() {
            return strValue0;
        }

        /**
         * @param strValue0 the strValue0 to set
         */
        public void setStrValue0(String strValue0) {
            this.strValue0 = strValue0;
        }

        /**
         * @return String return the intValue1
         */
        public String getStrValue1() {
            return strValue1;
        }

        /**
         * @param intValue1 the intValue1 to set
         */
        public void setStrValue1(String strValue1) {
            this.strValue1 = strValue1;
        }

        /**
         * @return TestData return the nested
         */
        public TestData getNested() {
            return nested;
        }

        /**
         * @param nested the nested to set
         */
        public void setNested(TestData nested) {
            this.nested = nested;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TestData) {
                TestData src = (TestData) obj;
                return this.intValue0 == src.intValue0 && this.intValue1 == src.intValue1
                        && objectEquals(this.strValue0, src.strValue0) && objectEquals(this.strValue1, src.strValue1)
                        && objectEquals(this.nested, src.nested);
            }
            return false;
        }
    }

    public static boolean objectEquals(Object objA, Object objB) {
        if (objA == null) {
            return objB == null;
        } else {
            return objA.equals(objB);
        }
    }

    public static int nextInt() {
        return new Random().nextInt();
    }

    public static String nextString(int length) {
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static TestData populate(TestData testData) {
        testData.setIntValue0(nextInt());
        testData.setIntValue1(nextInt());
        testData.setStrValue0(nextString(10));
        testData.setStrValue1(nextString(20));
        return testData;
    }

    public static TestData createTestData() {
        TestData testData = populate(new TestData());
        testData.setNested(populate(new TestData()));
        return testData;
    }

    private TokenizerServiceProvider tokenizerService;
    private ConfigurationServiceProvider configurationService;

    private class TestStringCypher implements StringCypher {
        private Charset charset;

        public TestStringCypher() {
            this.charset = Charset.forName("UTF-8");
        }

        private String bytesToString(byte[] bytes) {
            CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(bytes));
            StringBuffer stringBuffer = new StringBuffer(charBuffer);
            return stringBuffer.toString();
        }

        private byte[] stringToBytes(String text) {
            ByteBuffer byteBuffer = charset.encode(text);
            return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
        }

        @Override
        public String encrypt(String unsecure) throws CypherException {
            byte[] unsecureBytes = stringToBytes(unsecure);
            return Base64.getEncoder().encodeToString(unsecureBytes);
        }

        @Override
        public String decrypt(String secure) throws CypherException {
            byte[] unsecureBytes = Base64.getDecoder().decode(secure);
            return bytesToString(unsecureBytes);
        }

    }
    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    @Before
    public void setUp() throws Exception {
        // start the service
        File configFile = new File(
                locatePath(".\\tokenizer-service\\src\\test\\resources\\testConfig.yaml", "tokenizer-service"));
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

        KeyServiceProvider keyServiceProvider =_serviceProvider.provideService(KeyServiceProvider.class);
        if (!keyServiceProvider.isInitialized()) {
            try (Stage stage = keyServiceProvider.beginInitialize()) {
                keyServiceProvider.configure();
            }
        }

        DatastoreServiceProvider datastoreServiceProvider =_serviceProvider.provideService(DatastoreServiceProvider.class);
        if (!datastoreServiceProvider.isInitialized()) {
            try (Stage stage = datastoreServiceProvider.beginInitialize()) {
                datastoreServiceProvider.setCypher(new TestStringCypher());
                datastoreServiceProvider.configure();
            }
        }

        tokenizerService =_serviceProvider.provideService(TokenizerServiceProvider.class);
        if (!tokenizerService.isInitialized()) {
            try (Stage stage = tokenizerService.beginInitialize()) {
                tokenizerService.configure();
            }
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

    @After
    public void tearDown() throws Exception {
        if (tokenizerService != null) {
            tokenizerService.close();
        }
        if (configurationService != null) {
            configurationService.close();
        }
    }

    /**
     * test basic token creation
     */
    @Test
    public void basicTokenCreation() throws Exception {
        String token = tokenizerService.produceOpaqueToken("Hello World", Duration.ofMinutes(1));
        assertNotNull(token);
    }

    /**
     * test basic token creation and resolution
     */
    @Test
    public void basicTokenCreationAndResolution() throws Exception {
        String token = tokenizerService.produceOpaqueToken("Hello World", Duration.ofMinutes(1));
        assertNotNull(token);
        String actual = tokenizerService.consumeOpaqueToken(token, String.class);
        assertEquals("Token and resolution do not match", "Hello World", actual);
    }

    /**
     * test json token creation and resolution
     */
    @Test
    public void serializedTokenCreation() throws Exception {
        TestData testData = createTestData();
        String token = tokenizerService.produceOpaqueToken(testData, Duration.ofMinutes(1));
        assertNotNull(token);
    }

    /**
     * test json token creation and resolution
     */
    @Test
    public void serializedTokenCreationAndResolution() throws Exception {
        TestData testData = createTestData();
        String token = tokenizerService.produceOpaqueToken(testData, Duration.ofMinutes(1));
        assertNotNull(token);
        TestData actual = tokenizerService.consumeOpaqueToken(token, TestData.class);
        assertEquals("Token and resolution do not match", testData, actual);
    }

    /**
     * test json token creation and resolution
     */
    @Test
    public void removeAllExpired() throws Exception {
        long tokensRemoved = tokenizerService.removeAllExpired();
        assertTrue("zero or more tokens not removed", tokensRemoved >= 0);
    }

    @Test(expected = TokenizerException.class)
    public void testJWTInvalidType() throws Exception {
        Map<String, Object> requiredClaims = new HashMap<>();
        tokenizerService.produceWebToken("invalid-type", requiredClaims);
    }

    private static final String[] TEST_ROLES = { "Basic", "Admin", "Operator" };

    @SuppressWarnings("unchecked")
    private void testJsonWebToken(String tokenType) throws TokenizerException {
        Map<String, Object> requiredClaims = new HashMap<>();
        requiredClaims.put("first_name", "Fred");
        requiredClaims.put("last_name", "Flintstone");
        requiredClaims.put("user_name", "FredF");
        requiredClaims.put("email", "fredf@bedrock.com");
        List<String> testRoles = new ArrayList<String>();
        testRoles.add(TEST_ROLES[0]);
        testRoles.add(TEST_ROLES[1]);
        testRoles.add(TEST_ROLES[2]);
        requiredClaims.put("roles", testRoles);
        String token = tokenizerService.produceWebToken(tokenType, requiredClaims);

        AuthClaims actualClaims = tokenizerService.consumeWebToken(tokenType, token);

        for (Map.Entry<String, Object> entry : requiredClaims.entrySet()) {
            if (actualClaims.hasClaim(entry.getKey())) {
                String claimName = entry.getKey();
                Object requiredClaim = entry.getValue();
                Class<?> requiredClaimType = requiredClaim.getClass();
                Object actualClaim = actualClaims.getClaimValue(claimName);
                if (requiredClaimType.isInstance(actualClaim)) {
                    if (actualClaim instanceof String) {
                        assertTrue("String " + entry.getKey() + " claims are not the same",
                                requiredClaim.equals(actualClaim));
                    } else if (actualClaim instanceof List<?>) {
                        List<String> requiredClaimList = (List<String>) requiredClaim;
                        List<String> actualClaimList = (List<String>) actualClaim;
                        for (String actualClaimItem : actualClaimList) {
                            assertTrue("List<String> " + claimName + " claim does not contain " + actualClaimItem,
                                    requiredClaimList.contains(actualClaimItem));
                        }
                    } else {
                        assertTrue("claim " + claimName + " is not the same", requiredClaim.equals(actualClaim));
                    }
                } else {
                    assertTrue("claim " + claimName + " is not a " + requiredClaimType.getName(), false);
                }
            } else {
                assertTrue("missing claim " + entry.getKey(), false);
            }
        }
    }

    @Test
    public void testJWTTestTypeChaCha20() throws Exception {
        testJsonWebToken("chacha20");
    }

    @Test
    public void testJWTTypeAES() throws Exception {
        testJsonWebToken("aes");
    }

    @Test
    public void testJWTTypeOpaque() throws Exception {
        testJsonWebToken("opaque");
    }

    @Test
    public void testJWTTypePlainText() throws Exception {
        testJsonWebToken("plaintext");
    }

    @Test
    public void testGenerateNonce() throws Exception {
        String id = "3000a8bc-acf0-4bed-95bc-50037d91e994";
        String nonce = tokenizerService.produceNonce("server", id, Duration.ofSeconds(5));
        assertNotNull(nonce);
    }

    @Test
    public void testGenerateAndConsumeNonce() throws Exception {
        String id = "3000a8bc-acf0-4bed-95bc-50037d91e994";
        String nonce = tokenizerService.produceNonce("server", id, Duration.ofSeconds(5));
        tokenizerService.consumeNonce("server", nonce, Duration.ofDays(1));
    }

    @Test(expected = NonceReplayException.class)
    public void testGenerateAndConsumeReplayNonce() throws Exception {
        String id = "3000a8bc-acf0-4bed-95bc-50037d91e994";
        String nonce = tokenizerService.produceNonce("server", id, Duration.ofSeconds(5));
        tokenizerService.consumeNonce("server", nonce, Duration.ofDays(1));
        tokenizerService.consumeNonce("server", nonce, Duration.ofDays(1));
    }

    @Test
    public void testGenerateAndValidateNonceWithHash() throws Exception {
        String id = "3000a8bc-acf0-4bed-95bc-50037d91e994";
        String payload = "The quick brown fox jumped over the lazy dog";
        String nonce = tokenizerService.produceNonce("server", id, Duration.ofSeconds(5));
        String client_nonce = tokenizerService.produceNonce("client", id, Duration.ofSeconds(5));
        String hash = tokenizerService.produceNonceHash(nonce, client_nonce, payload);
        boolean validated = tokenizerService.consumeNonce("server", id, client_nonce, payload, hash);
        assertTrue("Nonce with hash was not validate", validated);
    }

    @Test(expected = NonceReplayException.class)
    public void testGenerateAndValidateNonceWithHashReplay() throws Exception {
        String id = "3000a8bc-acf0-4bed-95bc-50037d91e994";
        String payload = "The quick brown fox jumped over the lazy dog";
        String nonce = tokenizerService.produceNonce("server", id, Duration.ofSeconds(30));
        String client_nonce = tokenizerService.produceNonce("client", id, Duration.ofSeconds(5));
        String hash = tokenizerService.produceNonceHash(nonce, client_nonce, payload);
        boolean validated = tokenizerService.consumeNonce("server", id, client_nonce, payload, hash);
        assertTrue("Nonce with hash was not validate", validated);
        tokenizerService.consumeNonce("server", id, client_nonce, payload, hash);
    }

    @Test(expected = NonceExpiredException.class)
    public void testGenerateAndConsumeExpiredNonce() throws Exception {
        String id = "3000a8bc-acf0-4bed-95bc-50037d91e994";
        String payload = "The quick brown fox jumped over the lazy dog";
        String nonce = tokenizerService.produceNonce("server", id, Duration.ofMillis(250));
        String client_nonce = tokenizerService.produceNonce("client", id, Duration.ofSeconds(5));
        String hash = tokenizerService.produceNonceHash(nonce, client_nonce, payload);
        Thread.sleep(500);
        boolean validated = tokenizerService.consumeNonce("server", id, client_nonce, payload, hash);
        assertTrue("Nonce with hash was not validate", validated);
    }


}
