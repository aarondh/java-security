package org.daisleyharrison.security.services.configuration;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;

public class ConfigurationServiceTest {

    public static String TEST_CONFIGURATION_SOURCE = 
        "bool: true\n" + 
        "string: \"This is a string\"\n" + 
        "next:\n" + 
        "  bool: true\n" + 
        "  number1: 42\n" + 
        "  number2: 4.2\n" + 
        "  string: \"This is the next string\"\n" +
        "  duration: P1DT2H3M4.321S\n" +
        "number1: 42\n" + 
        "number2: 4.2\n" +
        "duration: P1DT2H3M4.321S\n";

    private ConfigurationServiceProvider configurationService;

    @Before
    public void setUp() throws Exception {
        // start the server
        configurationService = new ConfigurationService();
        try (InputStream inputStream = new ByteArrayInputStream(TEST_CONFIGURATION_SOURCE.getBytes())) {
            try(Stage stage = configurationService.beginInitialize()){
                configurationService.setSource(inputStream);
                configurationService.configure();
            }
        }

    }

    @After
    public void tearDown() throws Exception {
        configurationService.close();
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testString() {
        String value = configurationService.getValue("string", "fubar");
        assertTrue("This is a string".equals(value));
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testNextString() {
        String value = configurationService.getValue("next.string", "fubar");
        assertTrue("This is the next string".equals(value));
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testUndefinedStringValue1() {
        String value = configurationService.getValue("next.string.not", "fubar");
        assertTrue("fubar".equals(value));
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testUndefinedStringValue2() {
        String value = configurationService.getValue("undefined", "fubar");
        assertTrue("fubar".equals(value));
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testBooleanValue1() {
        boolean value = configurationService.getBooleanValue("bool", false);
        assertTrue(value);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testBooleanValue2() {
        boolean value = configurationService.getBooleanValue("next.bool", false);
        assertTrue(value);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testUndefinedBooleanValue() {
        boolean value = configurationService.getBooleanValue("undefined", false);
        assertFalse(value);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testIntegerValue1() {
        int value = configurationService.getIntegerValue("number1", -1);
        assertTrue(value == 42);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testIntegerValue2() {
        int value = configurationService.getIntegerValue("next.number1", -1);
        assertTrue(value == 42);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testUndefinedIntegerValue() {
        int value = configurationService.getIntegerValue("undefined", -1);
        assertTrue(value == -1);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testDoubleValue1() {
        double value = configurationService.getDoubleValue("number2", -1);
        assertTrue(value == 4.2);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testDoubleValue2() {
        double value = configurationService.getDoubleValue("next.number2", -1);
        assertTrue(value == 4.2);
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testUndefinedDoubleValue() {
        double value = configurationService.getDoubleValue("undefined", -1.0);
        assertTrue(value == -1.0);
    }
    /**
     * test retrieval of configuation data
     */
    @Test
    public void testDurationValue1() {
        Duration value = configurationService.getDurationValue("duration", Duration.parse("P4DT3H2M1.234S"));
        assertTrue(Duration.parse("P1DT2H3M4.321S").equals(value));
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testDurationValue2() {
        Duration value = configurationService.getDurationValue("next.duration", Duration.parse("P4DT3H2M1.234S"));
        assertTrue(Duration.parse("P1DT2H3M4.321S").equals(value));
    }

    /**
     * test retrieval of configuation data
     */
    @Test
    public void testUndefinedDurationValue() {
        Duration value = configurationService.getDurationValue("undefined", Duration.parse("P4DT3H2M1.234S"));
        assertTrue(Duration.parse("P4DT3H2M1.234S").equals(value));
    }

}
