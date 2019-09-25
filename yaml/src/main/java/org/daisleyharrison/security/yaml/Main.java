package org.daisleyharrison.security.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.cert.Certificate;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
    private static final String DEFAULT_PROPERTY = "signature";

    private static KeyStore getKeyStore(CommandLine commandLine)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        String keystorePath = null;
        String keyStoreType = "JKS";
        String password = null;
        if (commandLine.hasOption("keystore")) {
            keystorePath = commandLine.getOptionValue("keystore");

            if (commandLine.hasOption("p")) {
                password = commandLine.getOptionValue("p");
            } else {
                throw new IllegalArgumentException("Missing -p or -password (required when specifying --keystore)");
            }
            if (commandLine.hasOption("keystore-type")) {
                keyStoreType = commandLine.getOptionValue("keystore-type");
            }
            File keystoreFile = new File(keystorePath);
            System.out.println("using keystore file: " + keystoreFile.getAbsolutePath());
            try (InputStream inputStream = new FileInputStream(keystoreFile)) {
                final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(inputStream, password.toCharArray());
                return keyStore;
            }
        } else {
            throw new IllegalArgumentException("Missing -cert or -keystore");
        }
    }

    private static PublicKey getPublicKey(CommandLine commandLine) throws IOException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyException {
        String certPath = null;
        String keyAlias = null;
        if (commandLine.hasOption("c")) {
            certPath = commandLine.getOptionValue("c");
            File certFile = new File(certPath);
            System.out.println("using X509 cert file: " + certFile.getAbsolutePath());
            try (InputStream inputStream = new FileInputStream(certFile)) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                return certificate.getPublicKey();
            }
        } else {
            KeyStore keyStore = getKeyStore(commandLine);

            if (commandLine.hasOption("k")) {
                keyAlias = commandLine.getOptionValue("k");
            } else {
                throw new IllegalArgumentException("Missing -k or -key (required when specifying --keystore)");
            }
            Certificate cert = keyStore.getCertificate(keyAlias);
            if (cert == null) {
                throw new KeyException("key \"" + keyAlias + "\" was not found in the keystore");
            }
            return cert.getPublicKey();
        }
    }

    private static final String PEM_BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n";
    private static final String PEM_END_PRIVATE_KEY = "-----END PRIVATE KEY-----";

    private static PrivateKey getPrivateKey(CommandLine commandLine)
            throws IOException, KeyStoreException, InvalidKeySpecException, NoSuchAlgorithmException,
            CertificateException, KeyStoreException, KeyException, UnrecoverableKeyException {
        String keyAlias = null;
        String password = null;

        if (commandLine.hasOption("privatekey")) {
            String privateKeyFilePath = commandLine.getOptionValue("privatekey");
            File privateKeyFile = new File(privateKeyFilePath);
            if (privateKeyFile.exists()) {
                String privateKeyPemText = Files.readString(privateKeyFile.toPath());
                int beginPrivateKey = privateKeyPemText.indexOf(PEM_BEGIN_PRIVATE_KEY);
                int endPrivateKey = privateKeyPemText.indexOf(PEM_END_PRIVATE_KEY);
                if (beginPrivateKey < 0 || endPrivateKey < 0) {
                    throw new IllegalArgumentException("Private key file has invalid format (see openssl)");
                }
                beginPrivateKey += PEM_BEGIN_PRIVATE_KEY.length();
                String privateKeyText = privateKeyPemText.substring(beginPrivateKey, endPrivateKey);
                privateKeyText = privateKeyText.replace("\n", "");
                byte[] decoded = Base64.getDecoder().decode(privateKeyText);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePrivate(spec);
            } else {
                throw new FileNotFoundException("-privatekey " + privateKeyFile.getAbsolutePath() + " does not exist");
            }
        }

        KeyStore keyStore = getKeyStore(commandLine);

        if (commandLine.hasOption("k")) {
            keyAlias = commandLine.getOptionValue("k");
        } else {
            throw new IllegalArgumentException("Missing -k or -key (required when specifying --keystore)");
        }

        if (commandLine.hasOption("p")) {
            password = commandLine.getOptionValue("p");
        } else {
            throw new IllegalArgumentException("Missing -p or -password (required when specifying --keystore)");
        }

        Key key = keyStore.getKey(keyAlias, password.toCharArray());

        if (key == null) {
            throw new KeyException("key \"" + keyAlias + "\" was not found in the keystore");
        }

        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        } else {
            throw new KeyException("key \"" + keyAlias + "\" was a private key");
        }
    }

    public static InputStream getInputStream(CommandLine commandLine) throws FileNotFoundException {
        if (commandLine.hasOption("i")) {
            String inputPath = commandLine.getOptionValue("i");
            File inputFile = new File(inputPath);
            System.out.println("using input file: " + inputFile.getAbsolutePath());
            return new FileInputStream(inputFile);
        } else {
            throw new IllegalArgumentException("Missing -i or -input");
        }
    }

    public static OutputStream getOutputStream(CommandLine commandLine) throws FileNotFoundException {
        if (commandLine.hasOption("o")) {
            String outputPath = commandLine.getOptionValue("o");
            File outputFile = new File(outputPath);
            System.out.println("using output file: " + outputFile.getAbsolutePath());
            return new FileOutputStream(outputFile);
        } else {
            throw new IllegalArgumentException("Missing -o or -output");
        }
    }

    public static String getPropertyName(CommandLine commandLine) throws FileNotFoundException {
        if (commandLine.hasOption("property")) {
            return commandLine.getOptionValue("property");
        } else {
            return DEFAULT_PROPERTY;
        }
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(Option.builder().longOpt("keystore").hasArg()
                .desc("The keystore containing the private signing key").build());
        options.addOption(Option.builder().longOpt("privatekey").hasArg()
                .desc("The file containing the private signing key").build());
        options.addOption(Option.builder("k").longOpt("key").hasArg().desc("The name of the key used to sign").build());
        options.addOption(
                Option.builder("i").longOpt("input").hasArg().desc("The name of the yaml file to sign").build());
        options.addOption(
                Option.builder("o").longOpt("output").hasArg().desc("The name of the signed yaml file").build());
        options.addOption(Option.builder().longOpt("property").hasArg()
                .desc("The property name used to hold the signature").build());
        options.addOption(Option.builder("p").longOpt("password").hasArg()
                .desc("The password of the private key used to sign the yaml").build());
        options.addOption(Option.builder("c").longOpt("cert").hasArg()
                .desc("The public key cert used to verify the signature").build());
        options.addOption(Option.builder("h").longOpt("help").desc("Output this help message").build());
        options.addOption(Option.builder("v").longOpt("verify").desc("Verify the yaml signature").build());
        options.addOption(Option.builder("s").longOpt("sign").desc("Sign the yaml").build());
        options.addOption(Option.builder().longOpt("throw").desc("Throw an exception").build());

        boolean isThrowOnError = false;
        boolean isSigning = false;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);

            String propertyName = getPropertyName(commandLine);
            isThrowOnError = commandLine.hasOption("throw");
            if (commandLine.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("yamlSigner", options);
            } else if (commandLine.hasOption("s")) {
                isSigning = true;
                PrivateKey privateKey = getPrivateKey(commandLine);
                try (InputStream input = getInputStream(commandLine)) {
                    try (OutputStream output = getOutputStream(commandLine)) {
                        YamlSigningTools.sign(input, output, privateKey, propertyName);
                    }
                }
            } else if (commandLine.hasOption("v")) {
                PublicKey publicKey = getPublicKey(commandLine);
                try (InputStream input = getInputStream(commandLine)) {
                    boolean verified = YamlSigningTools.verifySignature(input, publicKey, propertyName);
                    if (verified) {
                        System.err.println("Signature verified");
                    } else {
                        throw new SignatureException("Invalid signature");
                    }
                }
            } else {
                throw new IllegalArgumentException("expected one of: -h, -s, or -v");
            }
        } catch (IllegalArgumentException exception) {
            System.err.println("Missing or invalid argument: " + exception.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("yamlSigner", options);
            if (isThrowOnError) {
                throw exception;
            }
        } catch (ParseException exception) {
            System.err.println(exception.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("yamlSigner", options);
            if (isThrowOnError) {
                throw exception;
            }
        } catch (Exception exception) {
            if (isSigning) {
                System.err.println("Signing failed: " + exception.getMessage());
            } else {
                System.err.println("Verification failed: " + exception.getMessage());
            }
            if (isThrowOnError) {
                throw exception;
            }
        }
    }
}