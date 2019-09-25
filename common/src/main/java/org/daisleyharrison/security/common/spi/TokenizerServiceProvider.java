package org.daisleyharrison.security.common.spi;

import org.daisleyharrison.security.common.exceptions.InvalidTokenException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.authorization.TokenMetaData;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

public interface TokenizerServiceProvider extends SecurityServiceProvider {
    public void configure();

    public String produceOpaqueToken(Object payload, Date expires, String tokenType) throws TokenizerException;

    public String produceOpaqueToken(Object payload, Date expires) throws TokenizerException;

    public String produceOpaqueToken(Object payload, Duration expires, String tokenType) throws TokenizerException;

    public String produceOpaqueToken(Object payload, Duration expires) throws TokenizerException;

    public String produceOpaqueStringToken(String payload, Date expires, String tokenType) throws TokenizerException;

    public String produceOpaqueStringToken(String payload, Date expires) throws TokenizerException;

    public String produceOpaqueStringToken(String payload, Duration expires, String tokenType) throws TokenizerException;

    public String produceOpaqueStringToken(String payload, Duration expires) throws TokenizerException;

    public String produceNonce(String namespaceName, String id, Duration expires) throws TokenizerException;

    public void consumeNonce(String namespaceName, String nonce, Duration expires) throws TokenizerException;

    public String produceNonceHash(String nonce, String client_nonce, String payload) throws TokenizerException;

    public boolean consumeNonce(String namespaceName, String id, String client_nonce, String payload, String hash)
            throws TokenizerException;

    public String getPublicJwk(String tokenType) throws TokenizerException;

    public <T> T consumeOpaqueToken(String token, Class<T> type) throws InvalidTokenException;

    public void expire(String token) throws InvalidTokenException;

    public String produceWebToken(String tokenType, Map<String, Object> userClaims) throws TokenizerException;

    public TokenMetaData getTokenMetaData(String tokenType);

    public AuthClaims consumeWebToken(String tokenType, String token) throws TokenizerException;
    
    public AuthClaims consumeWebTokenWithJwk(String tokenType, String token, String jwk) throws TokenizerException;

    public long removeAllExpired() throws TokenizerException;
}