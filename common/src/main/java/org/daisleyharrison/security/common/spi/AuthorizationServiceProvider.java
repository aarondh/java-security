package org.daisleyharrison.security.common.spi;

import org.daisleyharrison.security.common.exceptions.AuthorizationException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;

public interface AuthorizationServiceProvider extends SecurityServiceProvider {

    public String getIssuerForEmail(String email) throws AuthorizationException;

    public AuthClaims login(String email, char[] password) throws AuthorizationException;

    public AuthClaims loginByUserClaims(String issuer, AuthClaims userClaims) throws AuthorizationException;

    public void logout(AuthClaims userClaims) throws AuthorizationException;

    public AuthClaims createAccount(AuthClaims claims) throws AuthorizationException;

    public void deleteAccount(String accountId) throws AuthorizationException;

    public String getDomain();
}