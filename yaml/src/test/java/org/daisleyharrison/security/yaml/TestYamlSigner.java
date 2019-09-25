package org.daisleyharrison.security.yaml;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.SignatureException;

public class TestYamlSigner {
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

    private File yamlSrcFile;

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

    @Before
    public void setUp() throws Exception {
        yamlSrcFile = File.createTempFile("TestYamlSigner-", ".yaml");
        yamlSrcFile.deleteOnExit();
        try(OutputStream output = new FileOutputStream(yamlSrcFile)){
            OutputStreamWriter writer = new OutputStreamWriter(output);
            writer.write(TEST_CONFIGURATION_SOURCE);
            writer.flush();
        }        
    }

    @After
    public void tearDown() throws Exception {
        yamlSrcFile.delete();
    }
    /**
     * Test sign a yaml file using private key file (pem)
     */
    @Test
    public void testSignYamlUsingPem() throws Exception {
        String privateKeyPath = locatePath("./yaml/src/test/resources/yaml.signer.pem", "yaml");
        Main.main(new String[]{
            "--sign",
            "--input", yamlSrcFile.getAbsolutePath(),
            "--output", "TestYamlSigner.signed.yaml",
            "--privatekey", privateKeyPath,
            "--throw"
        });
    }
    /**
     * Test sign a yaml file using private key from a keystore (testkeystore.jks)
     */
    @Test
    public void testSignYamlUsingKeyStore() throws Exception {
        String keyStorePath = locatePath("./yaml/src/test/resources/testkeystore.jks", "yaml");
        Main.main(new String[]{
            "--sign",
            "--input", yamlSrcFile.getAbsolutePath(),
            "--output", "TestYamlSigner.signed.yaml",
            "--keystore", keyStorePath,
            "--password", "testyaml",
            "--key","yaml.signer",
            "--throw"
        });
    }
    /**
     * Test verify an signed valid yaml file
     */
    @Test
    public void testVerifyValidSignedYamlUsingCert() throws Exception {
        String certPath = locatePath("./yaml/src/test/resources/yaml.signer.crt", "yaml");
        String signedValidYamlPath = locatePath("./yaml/src/test/resources/TestYamlSigner.signed.valid.yaml", "yaml");
        Main.main(new String[]{
            "--verify",
            "--input", signedValidYamlPath,
            "--cert", certPath,
            "--throw"
        });
    }
    /**
     * Test verify an signed valid yaml file
     */
    @Test
    public void testVerifyValidSignedYamlUsingKeystore() throws Exception {
        String keyStorePath = locatePath("./yaml/src/test/resources/testkeystore.jks", "yaml");
        String signedValidYamlPath = locatePath("./yaml/src/test/resources/TestYamlSigner.signed.valid.yaml", "yaml");
        Main.main(new String[]{
            "--verify",
            "--input", signedValidYamlPath,
            "--keystore", keyStorePath,
            "--keystore", keyStorePath,
            "--password", "testyaml",
            "--key","yaml.signer",
            "--throw"
        });
    }
    /**
     * Test verify an signed invalid yaml file
     */
    @Test(expected = SignatureException.class)
    public void testVerifyInvalidSignedYamlUsingCert() throws Exception {
        String certPath = locatePath("./yaml/src/test/resources/yaml.signer.crt", "yaml");
        String signedInValidYamlPath = locatePath("./yaml/src/test/resources/TestYamlSigner.signed.invalid.yaml", "yaml");
        Main.main(new String[]{
            "--verify",
            "--input", signedInValidYamlPath,
            "--cert", certPath,
            "--throw"
        });
    }
    /**
     * Test verify an signed invalid yaml file
     */
    @Test(expected = SignatureException.class)
    public void testVerifyUnsignedYamlUsingCert() throws Exception {
        String certPath = locatePath("./yaml/src/test/resources/yaml.signer.crt", "yaml");
        String unsignedYamlPath = locatePath("./yaml/src/test/resources/TestYamlSigner.unsigned.yaml", "yaml");
        Main.main(new String[]{
            "--verify",
            "--input", unsignedYamlPath,
            "--cert", certPath,
            "--throw"
        });
    }
}
