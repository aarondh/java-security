package org.daisleyharrison.security.common.spi;

import java.security.Key;
import java.security.KeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.models.key.KeyFrame;
import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.Endorser;
import org.daisleyharrison.security.common.models.cypher.ConcealedString;
import org.daisleyharrison.security.common.models.cypher.Cypher;
import org.daisleyharrison.security.common.models.cypher.CypherAction;
import org.daisleyharrison.security.common.models.cypher.CypherFunc;
import org.daisleyharrison.security.common.models.cypher.HashCypher;

public interface CypherServiceProvider extends SecurityServiceProvider {

        public KeyFrame openFrame() throws CypherException;

        public <T> T processWithConcealedString(final ConcealedString concealedString, Class<T> type,
                        CypherFunc<char[]> action) throws CypherException;

        public void processWithKey(final String keyPath, CypherAction<Key> action) throws CypherException;

        public void processWithPublicKey(final String keyPath, CypherAction<PublicKey> action) throws CypherException;

        public void processWithPrivateKey(final String keyPath, CypherAction<PrivateKey> action) throws CypherException;

        public void processWithConcealedString(final ConcealedString concealedString, CypherAction<char[]> action)
        throws CypherException;

        public ConcealedString concealString(char[] unsecure, boolean internal) throws KeyException, CypherException;

        public ConcealedString concealString(char[] unsecure) throws KeyException, CypherException;

        public String generateSalt(final int length);

        public boolean keyExists(String keyPath);

        public Cypher getCypher(final String cypherName) throws CypherException;

        public StringCypher getStringCypher(final String cypherName) throws CypherException;

        public Endorser getEndorser(final String cypherName) throws CypherException;

        public HashCypher getHashCypher(final String cypherName) throws CypherException;

        public HashCypher getHashCypher(final String cypherName, byte[] salt) throws CypherException;

}
