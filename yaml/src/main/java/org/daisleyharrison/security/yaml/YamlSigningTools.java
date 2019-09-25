package org.daisleyharrison.security.yaml;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Map;

import java.util.Base64;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class YamlSigningTools {
    public static final String DEFAULT_SIGNATURE_PROPERTY_NAME = "signature";
    public static final String DEFAULT_CHARSET_NAME = "UTF8";
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * Convert string data to byte array
     * 
     * @param data        the string data
     * @param charSetName the character set name used to encode
     * @return byte[] the encoded bytes
     */
    private static byte[] toBytes(String data, String charSetName) {
        Charset charSet = Charset.forName(charSetName);
        ByteBuffer byteBuffer = charSet.encode(data);
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }

    /**
     * Sign byte[] data
     * 
     * @param data       the data to be signed
     * @param privateKey the private key used to sign the data
     * @return String signature
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static String sign(byte[] data, PrivateKey privateKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature rsa = Signature.getInstance(SIGNATURE_ALGORITHM);
        rsa.initSign(privateKey);
        rsa.update(data);

        byte[] signature = rsa.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    /**
     * Sign String data
     * 
     * @param data        the string to be signed
     * @param privateKey  the private key used to sign the data
     * @param charSetName the character set name used to encode
     * @return String signature
     * @return
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static String sign(String data, PrivateKey privateKey, String charSetName)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return sign(toBytes(data, charSetName), privateKey);
    }

    /**
     * Sign a yaml file
     * 
     * @param yaml       the snakeyaml object used to read the yaml file
     * @param input the input stream containing the yaml source
     * @param output the output stream the signed yaml will be written to
     * @param privateKey the private key used to sign the yaml
     * @return propertyName the top level yaml property name that will hold the signature
     * @return void
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static void sign(Yaml yaml, InputStream input, OutputStream output, PrivateKey privateKey,
            String propertyName) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException,
            ClassCastException, UnsupportedOperationException, IOException {

        Map<String, Object> yamlObject = yaml.load(input);
        if (yamlObject == null) {
            throw new IllegalArgumentException("input stream does not contain yaml");
        }
        yamlObject.remove(propertyName); // remove old signature property

        String yamlData = yaml.dump(yamlObject);

        String signature = sign(yamlData, privateKey, DEFAULT_CHARSET_NAME);

        yamlObject.put(propertyName, signature);

        OutputStreamWriter writer = new OutputStreamWriter(output);

        yaml.dump(yamlObject, writer);

        writer.flush();
    }
    /**
     * Sign a yaml file
     * 
     * @param yaml       the snakeyaml object used to read the yaml file
     * @param input the input stream containing the yaml source
     * @param output the output stream the signed yaml will be written to
     * @param privateKey the private key used to sign the yaml
     * @return void
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static void sign(Yaml yaml, InputStream input, OutputStream output, PrivateKey privateKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, ClassCastException,
            UnsupportedOperationException, IOException {
        sign(yaml, input, output, privateKey, DEFAULT_SIGNATURE_PROPERTY_NAME);
    }

    /**
     * Sign a yaml file
     * 
     * @param input the input stream containing the yaml source
     * @param output the output stream the signed yaml will be written to
     * @param privateKey the private key used to sign the yaml
     * @return propertyName the top level yaml property name that will hold the signature
     * @return void
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static void sign(InputStream input, OutputStream output, PrivateKey privateKey, String propertyName)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, ClassCastException,
            UnsupportedOperationException, IOException {
        Yaml yaml = new Yaml();
        sign(yaml, input, output, privateKey, propertyName);
    }
    /**
     * Sign a yaml file
     * 
     * @param input the input stream containing the yaml source
     * @param output the output stream the signed yaml will be written to
     * @param privateKey the private key used to sign the yaml
     * @return void
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static void sign(InputStream input, OutputStream output, PrivateKey privateKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, ClassCastException,
            UnsupportedOperationException, IOException {
        Yaml yaml = new Yaml();
        sign(yaml, input, output, privateKey);
    }
    /**
     * load and verify a signed yaml file
     * 
     * @param yaml       the snakeyaml object used to read the yaml file
     * @param input the input stream containing the signed yaml source
     * @param publicKey the public key used to verify the signature
     * @param propertyName the top level yaml propery in which to find the signature
     * @return the verified yaml object
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static Map<String, Object> loadAndVerifySignature(Yaml yaml, InputStream input, PublicKey publicKey,
            String propertyName) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Map<String, Object> yamlObject = yaml.load(input);
        if (yamlObject == null) {
            throw new IllegalArgumentException("input stream does not contain yaml");
        }
        Object signatureValue = yamlObject.get(propertyName);
        if (signatureValue == null || !(signatureValue instanceof String)) {
            throw new SignatureException("Invalid signature");
        }
        byte[] signature = Base64.getDecoder().decode((String) signatureValue);

        // remove the signature property as
        // it was not signed with it in place
        yamlObject.remove(propertyName);
        String yamlData = yaml.dump(yamlObject);
        byte[] data = toBytes(yamlData, DEFAULT_CHARSET_NAME);

        Signature rsa = Signature.getInstance(SIGNATURE_ALGORITHM);
        rsa.initVerify(publicKey);
        rsa.update(data);

        if (rsa.verify(signature)) {
            return yamlObject;
        }
        throw new SignatureException("Invalid signature");
    }
    /**
     * load and verify a signed yaml file
     * 
     * @param yaml       the snakeyaml object used to read the yaml file
     * @param input the input stream containing the signed yaml source
     * @param publicKey the public key used to verify the signature
     * @return the verified yaml object
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static Map<String, Object> loadAndVerifySignature(Yaml yaml, InputStream input, PublicKey publicKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return loadAndVerifySignature(yaml, input, publicKey, DEFAULT_SIGNATURE_PROPERTY_NAME);
    }
    /**
     * load and verify a signed yaml file
     * 
     * @param input the input stream containing the signed yaml source
     * @param publicKey the public key used to verify the signature
     * @param propertyName the top level yaml propery in which to find the signature
     * @return the verified yaml object
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public static boolean verifySignature(InputStream input, PublicKey publicKey, String propertyName)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Yaml yaml = new Yaml();
        try {
            loadAndVerifySignature(yaml, input, publicKey, propertyName);
            return true;
        } catch (SignatureException exception) {
            return false;
        }
    }
    /**
     * load and verify a signed yaml file
     * 
     * @param input the input stream containing the signed yaml source
     * @param publicKey the public key used to verify the signature
     * @return the verified yaml object
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */

    public static boolean verifySignature(InputStream input, PublicKey publicKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Yaml yaml = new Yaml();
        try {
            loadAndVerifySignature(yaml, input, publicKey);
            return true;
        } catch (SignatureException exception) {
            return false;
        }
    }

}