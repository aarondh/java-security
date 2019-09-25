package org.daisleyharrison.security.services.poptoken;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.spi.PopTokenConsumer;
import org.daisleyharrison.security.common.spi.PopTokenProducer;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.services.key.KeyService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.management.ServiceNotFoundException;

public class PopTokenFactoryTest {
    private static final LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    //@formatter:off
    public static String TEST_CONFIGURATION_SOURCE = 
        "key-service:\n" +
        "  keystores:\n" +
        "    root:\n" +
        "      keyPath: root/\n" +
        "      path: C:\\Projects\\org\\daisleyharrison\\security\\poptoken-factory\\src\\test\\resources\\cypherstore.jks\n" +
        "      type: JKS\n" +
        "      password: 123456\n" +
        "tokenizer:\n" +
        "  datastore:\n" +
        "    access: false\n" +
        "  token-types:\n" +
        "    PoP:\n" +
        "      type: jws\n" +
        "      alg-header: ES256\n" +
        "      key: root/client-keypair-ec256\n" +
        "      issuer: https://me.com\n" +
        "      audience: https://some.other.company.com\n" +
        "      expiry: 30\n" +
        "      not-before: 2";
    //@formatter:on

    private TokenizerServiceProvider tokenizerService;
    private KeyServiceProvider keyService;
    private ConfigurationServiceProvider configurationService;

    public PopTokenFactoryTest() throws ServiceNotFoundException {
    }

    @Before
    public void setUp() throws Exception {
        // start the configuration service
        configurationService = _serviceProvider.provideService(ConfigurationServiceProvider.class);
        try (InputStream inputStream = new ByteArrayInputStream(TEST_CONFIGURATION_SOURCE.getBytes())) {
            try (Stage stage = configurationService.beginInitialize()) {
                configurationService.setSource(inputStream);
                configurationService.configure();
            }
        }

        keyService = _serviceProvider.provideService(KeyServiceProvider.class);
        try (Stage stage = keyService.beginInitialize()) {
            keyService.configure();
        }

        tokenizerService = _serviceProvider.provideService(TokenizerServiceProvider.class);
        try (Stage stage = tokenizerService.beginInitialize()) {
            tokenizerService.configure();
        }
    }

    private Map<String, List<String>> createHeaders(String... nameValuePairs) {
        Map<String, List<String>> headers = new HashMap<>();
        if (nameValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must be pairs of name,value");
        }
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            String headerName = nameValuePairs[i];
            String headerValue = nameValuePairs[i + 1];
            List<String> values = headers.get(headerName);
            if (values == null) {
                values = new ArrayList<>();
                headers.put(headerName, values);
            }
            values.add(headerValue);
        }
        return headers;
    }

    /**
     * test the production of a pop token
     */
    @Test
    public void testProducePopToken() throws Exception {
        PopTokenFactory factory = new PopTokenFactory();
        PopTokenProducer producer = factory.getProducer();
        Map<String, List<String>> headers = createHeaders("Authorization", "Bearer 1234", "X-Auth-Originator",
                "Bearer 56789");
        URI uri = new URI("https://some.server.com/some/services?two=2&three=3&one=1&two=11");
        String body = "{\"this\": \"is\", \"a\": \"test\"}";
        Optional<String> token = producer.producePopToken("POST", headers, uri, body);
        assertTrue(token.isPresent());
    }

    /**
     * test the production of a pop token
     */
    @Test
    public void testConsumePopToken() throws Exception {
        PopTokenFactory factory = new PopTokenFactory();

        PopTokenConsumer consumer = factory.getConsumer();
        PopTokenProducer producer = factory.getProducer();

        Map<String, List<String>> headers = createHeaders("Authorization", "Bearer 1234", "X-Auth-Originator",
                "Bearer 56789");
        URI uri = new URI("https://some.server.com/some/services?two=2&three=3&one=1&two=11");
        String body = "{\"this\": \"is\", \"a\": \"test\"}";

        Optional<String> token = producer.producePopToken("POST", headers, uri, body);

        assertTrue(token.isPresent());

        String jwk = tokenizerService.getPublicJwk("PoP");

        //Recreate the uri but will the query params in a different order
        uri = new URI("https://some.server.com/some/services?three=3&two=2&two=11&one=1");

        consumer.consumePopToken("POST", headers, uri, body, token.get(), jwk);
    }
}
