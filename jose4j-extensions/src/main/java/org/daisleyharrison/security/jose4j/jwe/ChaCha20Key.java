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

import org.jose4j.lang.ByteUtil;

import javax.crypto.spec.SecretKeySpec;

/**
 */
public class ChaCha20Key extends SecretKeySpec
{
    public static final long serialVersionUID = -123421513452L;
    public static final String ALGORITHM = "ChaCha20";

    public ChaCha20Key(byte[] bytes)
    {
        super(bytes, ALGORITHM);
    }

    @Override
    public String toString()
    {
        return ByteUtil.bitLength(getEncoded().length) + " bit " + ALGORITHM + " key";
    }
}
