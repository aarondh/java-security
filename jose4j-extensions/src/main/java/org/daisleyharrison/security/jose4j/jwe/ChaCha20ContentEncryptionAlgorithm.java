/*
 * Copyright 2019 Aaron G. Daisley-Harrison
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
 * 
 * 
 * Based on the draft at https://tools.ietf.org/html/draft-amringer-jose-chacha-01
 * 
 * and RFC8439 https://tools.ietf.org/html/rfc8439
 * 
 */

package org.daisleyharrison.security.jose4j.jwe;

import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmInfo;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionParts;
import org.jose4j.jwx.Headers;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
/**

 *
 */
public class ChaCha20ContentEncryptionAlgorithm extends AlgorithmInfo implements ContentEncryptionAlgorithm
{
    private static final int TAG_BYTE_LENGTH = 128;
    private static final int INITIAL_COUNTER = 1;

    private ContentEncryptionKeyDescriptor contentEncryptionKeyDescriptor;
    private AeadCipher aeadCipher;
    private int ivByteLength;

    public ChaCha20ContentEncryptionAlgorithm(String alg, int keyBitLength, int ivByteLength)
    {
        setAlgorithmIdentifier(alg);
        setJavaAlgorithm(AeadCipher.CHACHA20_TRANSFORMATION_NAME);
        setKeyPersuasion(KeyPersuasion.SYMMETRIC);
        setKeyType(ChaCha20Key.ALGORITHM);
        contentEncryptionKeyDescriptor = new ContentEncryptionKeyDescriptor(ByteUtil.byteLength(keyBitLength), ChaCha20Key.ALGORITHM);
        aeadCipher = new AeadCipher(getJavaAlgorithm(), INITIAL_COUNTER, TAG_BYTE_LENGTH);
        
    }

    public ContentEncryptionKeyDescriptor getContentEncryptionKeyDescriptor()
    {
        return contentEncryptionKeyDescriptor;
    }

    public ContentEncryptionParts encrypt(byte[] plaintext, byte[] aad, byte[] contentEncryptionKey, Headers headers, byte[] ivOverride, ProviderContext providerContext)
            throws JoseException
    {
        byte[] iv = InitializationVectorHelp.iv(ivByteLength, ivOverride, providerContext.getSecureRandom());
        String cipherProvider = ContentEncryptionHelp.getCipherProvider(headers, providerContext);
        return encrypt(plaintext, aad, contentEncryptionKey, iv, cipherProvider);
    }



    public ContentEncryptionParts encrypt(byte[] plaintext, byte[] aad, byte[] contentEncryptionKey, byte[] iv, String provider)
            throws JoseException
    {
        ChaCha20Key cek = new ChaCha20Key(contentEncryptionKey);
        AeadCipher.CipherOutput encrypted = aeadCipher.encrypt(cek, iv, plaintext, aad, provider);
        return new ContentEncryptionParts(iv, encrypted.getCiphertext(), encrypted.getTag());
    }

    public byte[] decrypt(ContentEncryptionParts contentEncParts, byte[] aad, byte[] contentEncryptionKey, Headers headers, ProviderContext providerContext)
            throws JoseException
    {
        byte[] iv = contentEncParts.getIv();
        ChaCha20Key cek = new ChaCha20Key(contentEncryptionKey);
        byte[] ciphertext = contentEncParts.getCiphertext();
        byte[] tag = contentEncParts.getAuthenticationTag();
        String cipherProvider = ContentEncryptionHelp.getCipherProvider(headers, providerContext);
        return aeadCipher.decrypt(cek, iv, ciphertext, tag, aad, cipherProvider);
    }

    /**
     * @param ivByteLength the ivByteLength to set
     */
    public void setIvByteLength(int ivByteLength) {
        this.ivByteLength = ivByteLength;
    }

    /**
     * @return the ivByteLength
     */
    public int getIvByteLength() {
        return ivByteLength;
    }

    @Override
    public boolean isAvailable()
    {
        return aeadCipher.isAvailable(log, getAlgorithmIdentifier());
    }

    public static class ChaCha20Poly1305 extends ChaCha20ContentEncryptionAlgorithm
    {
        public ChaCha20Poly1305()
        {
            super(ExtendedContentEncryptionAlgorithmIdentifiers.AEAD_CHACHA20_POLY1305, 256, 96);
        }
    }

    public static class XChaCha20Poly1305 extends ChaCha20ContentEncryptionAlgorithm
    {
        public XChaCha20Poly1305()
        {
            super(ExtendedContentEncryptionAlgorithmIdentifiers.AEAD_XCHACHA20_POLY1305, 256, 192);
        }
    }

}