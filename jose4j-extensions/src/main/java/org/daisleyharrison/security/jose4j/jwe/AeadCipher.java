/*
 * Copyright 2019 Aaron G Daisley-Harrison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.daisleyharrison.security.jose4j.jwe;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.ExceptionHelp;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;

/**
 * 1
 */
public class AeadCipher {
    public static final String CHACHA20_TRANSFORMATION_NAME = "ChaCha20-Poly1305";

    private String algorithm;
    private int counter;
    private int tagByteLength;

    public AeadCipher(String algorithm, int counter, int tagByteLength) {
        this.algorithm = algorithm;
        this.counter = counter;
        this.tagByteLength = tagByteLength;
    }

    private Cipher getInitialisedCipher(Key key, byte[] iv, int mode, String provider) throws JoseException {
        try {
            Cipher cipher = Cipher.getInstance(CHACHA20_TRANSFORMATION_NAME);
            // ChaCha20ParameterSpec paramSpec = new ChaCha20ParameterSpec(iv, counter);
            // cipher.init(mode, key, paramSpec);
            IvParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(mode, key, paramSpec);
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            throw new JoseException("No such algorithm for " + algorithm, e);
        } catch (NoSuchPaddingException e) {
            throw new JoseException("No such padding for " + algorithm, e);
        } catch (java.security.InvalidKeyException e) {
            throw new JoseException("Invalid key for " + algorithm, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new JoseException(e.toString(), e);
        }
    }

    public CipherOutput encrypt(Key key, byte[] iv, byte[] plaintext, byte[] aad, String provider)
            throws JoseException {
        Cipher cipher = getInitialisedCipher(key, iv, Cipher.ENCRYPT_MODE, provider);
        updateAad(cipher, aad);

        byte[] cipherOutput;
        try {
            cipherOutput = new byte[cipher.getOutputSize(plaintext.length)];
            cipher.doFinal(plaintext, 0, plaintext.length, cipherOutput);
        } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
            throw new JoseException(e.toString(), e);
        }

        CipherOutput result = new CipherOutput();
        int tagIndex = cipherOutput.length - tagByteLength;
        result.ciphertext = ByteUtil.subArray(cipherOutput, 0, tagIndex);
        result.tag = ByteUtil.subArray(cipherOutput, tagIndex, tagByteLength);
        return result;
    }

    private void updateAad(Cipher cipher, byte[] aad) {
        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }
    }

    public byte[] decrypt(Key key, byte[] iv, byte[] ciphertext, byte[] tag, byte[] aad, String provider)
            throws JoseException {
        Cipher cipher = getInitialisedCipher(key, iv, Cipher.DECRYPT_MODE, provider);
        updateAad(cipher, aad);

        try {
            return cipher.doFinal(ByteUtil.concat(ciphertext, tag));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new JoseException(e.toString(), e);
        }

    }

    public boolean isAvailable(Logger log, String joseAlg) {
        boolean isAvailable = false;
        try {
            Cipher.getInstance(CHACHA20_TRANSFORMATION_NAME);
            isAvailable = true;
        } catch (Throwable e) {
            log.debug("{} is not available ({}).", joseAlg, ExceptionHelp.toStringWithCauses(e));
        }
        return isAvailable;
    }

    public static class CipherOutput {
        private byte[] ciphertext;
        private byte[] tag;

        public byte[] getCiphertext() {
            return ciphertext;
        }

        public byte[] getTag() {
            return tag;
        }
    }
}