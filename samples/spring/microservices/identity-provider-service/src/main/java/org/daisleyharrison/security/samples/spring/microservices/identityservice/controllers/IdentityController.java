package org.daisleyharrison.security.samples.spring.microservices.identityservice.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.authorization.Claims;
import org.daisleyharrison.security.common.models.authorization.OpenIdClaims;
import org.daisleyharrison.security.common.models.authorization.TokenMetaData;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.common.utilities.AuthClaimsImpl;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AccessToken;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AccessTokenRequest;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AccessTokenResponse;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthenticationClient;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthenticationClientProvider;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthorizationRequest;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.CodeClaims;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthorizationResponse;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.OAuth2Error;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.OAuth2Exception;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.Scope;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.TaapClaims;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.UserProvider;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.ValidateRequest;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.ValidateRequest.ReturnTypes;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.AllowAnonymous;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.AllowWithAuthority;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.JwtAuthenticationToken;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.Taap;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/identity")
public class IdentityController {
    private static final String SIGNED_ID_TOKEN_TYPE = "signedIdToken";
    private static final String ENCRYPTED_ID_TOKEN_TYPE = "encryptedIdToken";
    private static final String ENCRYPTED_ACCESS_TOKEN_TYPE = "encryptedIdToken";
    private static final String ENCRYPTED_CODE_TOKEN_TYPE = "encryptedIdToken";
    private static final String IDENTITY_ISSUER = "daisleyharrison.com";
    private static final String IDENTITY_AUDIENCE = "UNKONWN";

    @Autowired
    AuthenticationClientProvider clientProvider;

    @Autowired
    UserProvider userProvider;

    @Autowired
    TokenizerServiceProvider tokenizerService;

    public IdentityController() {
    }

    @AllowAnonymous
    @GetMapping("/authenticate")
    public ResponseEntity<AuthorizationResponse> getAuthenticate(AuthorizationRequest authorizationRequest)
            throws OAuth2Exception {
        String clientId = authorizationRequest.getClientId();
        if (clientId == null) {
            throw new OAuth2Exception(OAuth2Error.UNAUTHORIZED_CLIENT, "client_id was not supplied",
                    authorizationRequest.getState());
        }

        AuthenticationClient client = clientProvider.find(clientId);
        if (client == null) {
            throw new OAuth2Exception(OAuth2Error.UNAUTHORIZED_CLIENT, "Unauthorized client",
                    authorizationRequest.getState());
        }
        if (client.isAllowsGetMethod()) {
            return postAuthenticate(authorizationRequest);
        } else {
            throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "Use of GET method not allowed",
                    authorizationRequest.getState());
        }
    }

    @AllowAnonymous
    @PostMapping("/authenticate")
    public ResponseEntity<AuthorizationResponse> postAuthenticate(
            @RequestBody AuthorizationRequest authorizationRequest) throws OAuth2Exception {
        try {
            if (!Arrays.asList(authorizationRequest.getResponseTypes()).contains("code")) {
                throw new OAuth2Exception(OAuth2Error.UNSUPPORTED_RESPONSE_TYPE, "The response type was not recognized",
                        authorizationRequest.getState());
            }
            if (Arrays.asList(authorizationRequest.getResponseTypes()).contains("idtoken")) {
                throw new OAuth2Exception(OAuth2Error.UNSUPPORTED_RESPONSE_TYPE, "The response type not supported",
                        authorizationRequest.getState());
            }
            String clientId = authorizationRequest.getClientId();
            if (clientId == null) {
                throw new OAuth2Exception(OAuth2Error.UNAUTHORIZED_CLIENT, "client_id was not supplied",
                        authorizationRequest.getState());
            }

            AuthenticationClient client = clientProvider.find(clientId);
            if (client == null) {
                throw new OAuth2Exception(OAuth2Error.UNAUTHORIZED_CLIENT, "Unauthorized client",
                        authorizationRequest.getState());
            }

            OpenIdClaims endUser = null;
            String username = authorizationRequest.getLoginHint();
            if (username != null) {
                // TODO this should be a call to authenticate here
                endUser = userProvider.find(username);
                if (endUser == null) {
                    throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Invalid login_hint",
                            authorizationRequest.getState());
                }
            }
            AuthorizationResponse response = new AuthorizationResponse();

            response.setState(authorizationRequest.getState());

            // Identity validation occurs here

            CodeClaims codeClaims = new CodeClaims();
            codeClaims.setGeneratedJwtId();
            codeClaims.setIssuer(IDENTITY_ISSUER);
            codeClaims.addAudience(IDENTITY_ISSUER);
            codeClaims.setClaim(CodeClaims.ReservedClaimNames.CLIENT_ID, clientId);

            codeClaims.setClaim(CodeClaims.ReservedClaimNames.CONFIRMATION, authorizationRequest.getConfirmation());
            codeClaims.setClaim(CodeClaims.ReservedClaimNames.CODE_CHALLENGE, authorizationRequest.getCodeChallenge());
            codeClaims.setClaim(CodeClaims.ReservedClaimNames.CODE_CHALLENGE_METHOD,
                    authorizationRequest.getCodeChallengeMethod());
            codeClaims.setClaim(CodeClaims.ReservedClaimNames.SCOPE, authorizationRequest.getScope());
            codeClaims.setClaim(CodeClaims.ReservedClaimNames.REDIRECT_URI, authorizationRequest.getRedirectUri());
            codeClaims.setClaim(CodeClaims.ReservedClaimNames.NONCE, authorizationRequest.getNonce());
            codeClaims.setClaim(CodeClaims.ReservedClaimNames.STATE, authorizationRequest.getState());

            String codeToken = tokenizerService.produceWebToken(ENCRYPTED_CODE_TOKEN_TYPE, codeClaims.getClaimsMap());

            response.setNonce(authorizationRequest.getNonce());
            response.setState(authorizationRequest.getState());
            response.setCode(codeToken);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (MalformedAuthClaimException | TokenizerException exception) {
            throw new OAuth2Exception(OAuth2Error.SERVER_ERROR, exception, authorizationRequest.getState());
        }
    }

    private List<String> getGrantedScopes(OpenIdClaims user, String requestedScope) {
        List<String> grantedScopes = new ArrayList<>();
        try {
            if (requestedScope != null) {
                String[] requestedScopes = requestedScope.split("\\s+");
                for (String scopeRequested : requestedScopes) {
                    if (user.hasScope(scopeRequested)) {
                        grantedScopes.add(scopeRequested);
                    }
                }
            }
        } catch (MalformedAuthClaimException ex) {

        }
        return grantedScopes;
    }

    private List<String> getGrantedScopes(AuthenticationClient client, String requestedScope) {
        List<String> grantedScopes = new ArrayList<>();
        if (requestedScope != null) {
            String[] requestedScopes = requestedScope.split("\\s+");
            for (String scopeRequested : requestedScopes) {
                if (client.hasScope(scopeRequested)) {
                    grantedScopes.add(scopeRequested);
                }
            }
        }
        return grantedScopes;
    }

    private ResponseEntity<AccessTokenResponse> handleAuthorizationCodeGrantType(AccessTokenRequest accessTokenRequest)
            throws OAuth2Exception {
        String state = null;
        try {

            String codeToken = accessTokenRequest.getCode();

            if (codeToken == null) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "code was not supplied");
            }

            AuthClaims codeClaims = tokenizerService.consumeWebToken(ENCRYPTED_CODE_TOKEN_TYPE, codeToken);

            state = codeClaims.getStringClaimValue(CodeClaims.ReservedClaimNames.STATE);

            String clientId = codeClaims.getStringClaimValue(CodeClaims.ReservedClaimNames.CLIENT_ID);
            AuthenticationClient client = clientProvider.find(clientId);
            if (client == null) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access denied");
            }

            String username = codeClaims.getSubject();
            String password = codeClaims.getStringClaimValue(CodeClaims.ReservedClaimNames.PASSWORD);
            OpenIdClaims endUser;
            try {
                endUser = userProvider.authenticate(username, password);
            } catch (SecurityException ex) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access denied");
            }

            String redirectUri = codeClaims.getStringClaimValue(CodeClaims.ReservedClaimNames.REDIRECT_URI);

            if (redirectUri != null && (!redirectUri.equals(accessTokenRequest.getRedirectUri())
                    || !client.hasRedirectUri(redirectUri))) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "invalid redirect_uri");
            }

            if (!"authorization_code".equals(accessTokenRequest.getGrantType())) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "grant_type is not authroization_code");
            }
            String requestedScope = codeClaims.getStringClaimValue(CodeClaims.ReservedClaimNames.SCOPE);
            List<String> grantedScopes = getGrantedScopes(endUser, requestedScope);

            AccessTokenResponse response = new AccessTokenResponse();
            response.setTokenType("access_token");

            OpenIdClaims idClaims = new AuthClaimsImpl();
            idClaims.setGeneratedJwtId();
            idClaims.setIssuer(IDENTITY_ISSUER);
            idClaims.addAudience(client.getDomain());
            idClaims.setSubject(codeClaims.getSubject());
            idClaims.setSubject(codeClaims.getSubject());
            idClaims.setEmail(endUser.getEmail());
            OpenIdClaims accessClaims = new AuthClaimsImpl();
            accessClaims.setGeneratedJwtId();
            accessClaims.setIssuer(IDENTITY_ISSUER);
            accessClaims.addAudience(client.getDomain());
            accessClaims.setSubject(codeClaims.getSubject());

            accessClaims.addScope(grantedScopes);

            String cnf = codeClaims.getStringClaimValue(CodeClaims.ReservedClaimNames.CONFIRMATION);
            if (cnf != null) {
                accessClaims.setClaim(TaapClaims.ReservedClaimNames.CONFIRMATION, accessTokenRequest.getConfirmation());
                idClaims.setClaim(TaapClaims.ReservedClaimNames.CONFIRMATION, accessTokenRequest.getConfirmation());
            }

            TokenMetaData tokenMetaData = tokenizerService.getTokenMetaData(ENCRYPTED_ACCESS_TOKEN_TYPE);
            String idToken = tokenizerService.produceWebToken(ENCRYPTED_ID_TOKEN_TYPE, idClaims.getClaimsMap());

            String accessToken = tokenizerService.produceWebToken(ENCRYPTED_ACCESS_TOKEN_TYPE,
                    accessClaims.getClaimsMap());

            response.setScope(String.join(" ", codeClaims.getScopes()));
            int expiresInMinutes = tokenMetaData.getExpires();
            if (expiresInMinutes > 0) {
                response.setExpiresIn(Long.toString(expiresInMinutes * 60));
            }
            response.setAccessToken(accessToken);

            response.setIdToken(idToken);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (MalformedAuthClaimException | TokenizerException exception) {
            throw new OAuth2Exception(OAuth2Error.SERVER_ERROR, exception, state);
        }
    }

    private ResponseEntity<AccessTokenResponse> handlePasswordGrantType(AccessTokenRequest accessTokenRequest)
            throws OAuth2Exception {
        try {
            AuthenticationClient client = null;
            OpenIdClaims endUser = null;

            if (!"password".equals(accessTokenRequest.getGrantType())) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "grant_type is not password");
            }

            String client_id = accessTokenRequest.getClientId();
            if (client_id == null) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "client_id was not supplied");
            }

            String client_secret = accessTokenRequest.getClientSecret();
            if (client_secret == null) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            try {
                client = clientProvider.authenticate(client_id, client_secret);
            } catch (SecurityException ex) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            String username = accessTokenRequest.getUsername();
            if (username == null) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            String password = accessTokenRequest.getPassword();
            if (password == null) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            try {
                endUser = userProvider.authenticate(username, password);
            } catch (SecurityException ex) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            String redirectUri = accessTokenRequest.getRedirectUri();

            if (redirectUri != null && !client.hasRedirectUri(redirectUri)) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "invalid redirect_uri");
            }

            String requestedScope = accessTokenRequest.getScope();
            List<String> grantedScopes = getGrantedScopes(endUser, requestedScope);

            AccessTokenResponse response = new AccessTokenResponse();
            response.setTokenType("access_token");

            AuthClaims accessClaims = new AuthClaimsImpl();
            accessClaims.setGeneratedJwtId();
            accessClaims.setIssuer(IDENTITY_ISSUER);
            accessClaims.addAudience(client.getDomain());
            accessClaims.setSubject(endUser.getSubject());
            accessClaims.addScope(grantedScopes);
            String cnf = accessTokenRequest.getConfirmation();
            if (cnf != null) {
                accessClaims.setClaim(CodeClaims.ReservedClaimNames.CONFIRMATION, cnf);
            }

            TokenMetaData tokenMetaData = tokenizerService.getTokenMetaData(ENCRYPTED_ACCESS_TOKEN_TYPE);

            String accessToken = tokenizerService.produceWebToken(ENCRYPTED_ACCESS_TOKEN_TYPE,
                    accessClaims.getClaimsMap());

            response.setScope(String.join(" ", accessClaims.getScopes()));

            int expiresInMinutes = tokenMetaData.getExpires();
            if (expiresInMinutes > 0) {
                response.setExpiresIn(Long.toString(expiresInMinutes * 60));
            }

            response.setAccessToken(accessToken);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (MalformedAuthClaimException | TokenizerException exception) {
            throw new OAuth2Exception(OAuth2Error.SERVER_ERROR, exception, null);
        }
    }

    private ResponseEntity<AccessTokenResponse> handleClientCredentialsGrantType(AccessTokenRequest accessTokenRequest)
            throws OAuth2Exception {
        try {
            if (!AccessTokenRequest.GrantTypes.CLIENT_CREDENTIALS.equals(accessTokenRequest.getGrantType())) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST,
                        "grant_type is not " + AccessTokenRequest.GrantTypes.CLIENT_CREDENTIALS);
            }

            String client_id = accessTokenRequest.getClientId();
            if (client_id == null) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "client_id was not supplied");
            }

            String client_secret = accessTokenRequest.getClientSecret();
            if (client_secret == null) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            AuthenticationClient client;
            try {
                client = clientProvider.authenticate(client_id, client_secret);
            } catch (SecurityException ex) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            String redirectUri = accessTokenRequest.getRedirectUri();

            if (redirectUri != null && !client.hasRedirectUri(redirectUri)) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "invalid redirect_uri");
            }

            String requestedScope = accessTokenRequest.getScope();
            List<String> grantedScopes = getGrantedScopes(client, requestedScope);

            AccessTokenResponse response = new AccessTokenResponse();
            response.setTokenType("access_token");

            AuthClaims accessClaims = new AuthClaimsImpl();
            accessClaims.setGeneratedJwtId();
            accessClaims.setIssuer(IDENTITY_ISSUER);
            accessClaims.addAudience(client.getDomain());
            accessClaims.setSubject(client.getClientId());
            accessClaims.addScope(grantedScopes);
            String cnf = accessTokenRequest.getConfirmation();
            if (cnf != null) {
                accessClaims.setClaim(CodeClaims.ReservedClaimNames.CONFIRMATION, cnf);
            }

            TokenMetaData tokenMetaData = tokenizerService.getTokenMetaData(ENCRYPTED_ACCESS_TOKEN_TYPE);

            String accessToken = tokenizerService.produceWebToken(ENCRYPTED_ACCESS_TOKEN_TYPE,
                    accessClaims.getClaimsMap());

            response.setScope(String.join(" ", accessClaims.getScopes()));

            int expiresInMinutes = tokenMetaData.getExpires();
            if (expiresInMinutes > 0) {
                response.setExpiresIn(Long.toString(expiresInMinutes * 60));
            }

            response.setAccessToken(accessToken);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (MalformedAuthClaimException | TokenizerException exception) {
            throw new OAuth2Exception(OAuth2Error.SERVER_ERROR, exception, null);
        }
    }

    private ResponseEntity<AccessTokenResponse> handleExtensionGrants(AccessTokenRequest accessTokenRequest,
            URI extensionGrantUri) throws OAuth2Exception {
        throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "unsupported grant_type");
    }

    @AllowAnonymous
    @PostMapping("/token")
    public ResponseEntity<AccessTokenResponse> postToken(@RequestBody AccessTokenRequest accessTokenRequest)
            throws OAuth2Exception {

        try {
            String grantType = accessTokenRequest.getGrantType();
            if (grantType == null) {
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "grant_type missing");
            } else if (grantType.equals(AccessTokenRequest.GrantTypes.AUTHROIZATION_CODE)) {
                return handleAuthorizationCodeGrantType(accessTokenRequest);
            } else if (grantType.equals(AccessTokenRequest.GrantTypes.PASSWORD)) {
                return handlePasswordGrantType(accessTokenRequest);
            } else if (grantType.equals(AccessTokenRequest.GrantTypes.CLIENT_CREDENTIALS)) {
                return handleClientCredentialsGrantType(accessTokenRequest);
            } else {
                URI extensionGrantUri = URI.create(grantType);
                if (extensionGrantUri.isAbsolute()) {
                    return handleExtensionGrants(accessTokenRequest, extensionGrantUri);
                }
                throw new OAuth2Exception(OAuth2Error.INVALID_REQUEST, "unsupported grant_type");
            }
        } catch (Exception exception) {
            throw new OAuth2Exception(OAuth2Error.SERVER_ERROR, exception, null);
        }

    }

    public static class Claim {
        private String name;
        private Object value;

        public Claim(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }

    private interface ClaimsProvider {
        Claim[] claims(OpenIdClaims claimSource, String scope);
    }

    private static final Map<String, ClaimsProvider> s_claimsProviders = new HashMap<>();

    private static boolean addClaimIfFound(OpenIdClaims source, String claimName, List<Claim> claims){
        Object value = source.getClaimValue(claimName);
        if(value== null){
            return false;
        }
        claims.add(new Claim(claimName, value));
        return true;
    }

    private static Claim[] returnClaimIfFound(OpenIdClaims source, String claimName){
        Object value = source.getClaimValue(claimName);
        if(value== null){
            return null;
        }
        Claim[] claims = { new Claim(claimName, value) };
        return claims;

        }

    static {

        s_claimsProviders.put("profile", (claimSource, scope) -> {
            List<Claim> claims = new ArrayList<>();
            addClaimIfFound(claimSource,"name", claims);
            addClaimIfFound(claimSource,"family_name", claims);
            addClaimIfFound(claimSource,"given_name", claims);
            addClaimIfFound(claimSource,"middle_name", claims);
            addClaimIfFound(claimSource,"nickname", claims);
            addClaimIfFound(claimSource,"preferred_username", claims);
            addClaimIfFound(claimSource,"profile", claims);
            addClaimIfFound(claimSource,"picture", claims);
            addClaimIfFound(claimSource,"given_name", claims);
            addClaimIfFound(claimSource,"website", claims);
            addClaimIfFound(claimSource,"gender", claims);
            addClaimIfFound(claimSource,"birthdate", claims);
            addClaimIfFound(claimSource,"zoneinfo", claims);
            addClaimIfFound(claimSource,"locale", claims);
            addClaimIfFound(claimSource,"updated_at", claims);
            return claims.toArray(Claim[]::new);
        });

        s_claimsProviders.put("phone", (claimSource, scope) -> {
            List<Claim> claims = new ArrayList<>();
            addClaimIfFound(claimSource,"phone_number", claims);
            addClaimIfFound(claimSource,"phone_number_verified", claims);
            return claims.toArray(Claim[]::new);
        });

        s_claimsProviders.put("email", (claimSource, scope) -> {
            List<Claim> claims = new ArrayList<>();
            addClaimIfFound(claimSource,"email", claims);
            addClaimIfFound(claimSource,"email_verified", claims);
            return claims.toArray(Claim[]::new);
        });
    }

    private Claim[] getClaimsForScope(OpenIdClaims source, String scope) {
        ClaimsProvider claimsProvider = s_claimsProviders.get(scope);
        if (claimsProvider == null) {

            return returnClaimIfFound(source, scope);
        }
        return claimsProvider.claims(source, scope);
    }

    private AuthClaims getUserClaims(OpenIdClaims sourceClaims, List<String> audience, List<String> scopeNames)
            throws OAuth2Exception {
        try {

            if (sourceClaims == null) {
                throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
            }

            AuthClaims userClaims = new AuthClaimsImpl();
            userClaims.setGeneratedJwtId();
            userClaims.setIssuer(IDENTITY_ISSUER);
            userClaims.addAudience(audience);
            userClaims.setSubject(userClaims.getSubject());

            for (String scopeName : scopeNames) {
                Claim[] claims = getClaimsForScope(sourceClaims, scopeName);
                if (claims == null) {
                    if (sourceClaims.hasScope(scopeName)) {
                        userClaims.addScope(scopeName);
                    }
                } else {
                    for (Claim claim : claims) {
                        userClaims.setClaim(claim.getName(), claim.getValue());
                    }
                }
            }

            return userClaims;
        } catch (Exception exception) {
            throw new OAuth2Exception(OAuth2Error.SERVER_ERROR, exception, null);
        }
    }

    @GetMapping("/userInfo")
    public ResponseEntity<String> getUserInfo(Authentication authentication) throws OAuth2Exception {
        if (authentication instanceof JwtAuthenticationToken<?>) {
            try {
                JwtAuthenticationToken<?> jwtAuthenticationToken = (JwtAuthenticationToken<?>) authentication;
                AuthClaims userClaims = jwtAuthenticationToken.getClaims();
                OpenIdClaims user = userProvider.find(userClaims.getSubject());
                if (user == null) {
                    throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied");
                }

                AuthClaims claims = getUserClaims(user, userClaims.getAudience(), userClaims.getScopes());

                return ResponseEntity.ok(claims.toJson());
            } catch (Exception exception) {
                throw new OAuth2Exception(OAuth2Error.SERVER_ERROR, exception, null);
            }
        }
        throw new OAuth2Exception(OAuth2Error.ACCESS_DENIED, "Access Denied", null);
    }

    @AllowAnonymous
    @GetMapping("/certs")
    public ResponseEntity<String> getCerts() {
        try {
            List<JsonWebKey> keys = new ArrayList<>();

            try {
                String key = tokenizerService.getPublicJwk(SIGNED_ID_TOKEN_TYPE);
                JsonWebKey jwk = JsonWebKey.Factory.newJwk(key);
                keys.add(jwk);
            } catch (TokenizerException exception) {
            }
            JsonWebKeySet jwks = new JsonWebKeySet(keys);
            return new ResponseEntity<>(jwks.toJson(), HttpStatus.OK);
        } catch (JoseException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> postValidate(Authentication authentication,
            @RequestBody ValidateRequest validateRequest) {
        try {
            AuthClaims claims = tokenizerService.consumeWebToken(ENCRYPTED_ID_TOKEN_TYPE, validateRequest.getToken());
            ReturnTypes return_type = validateRequest.getReturnType();
            if (return_type == ReturnTypes.CLAIMS) {
                return new ResponseEntity<>(claims.toJson(), HttpStatus.OK);
            } else if (return_type == ReturnTypes.JWS) {
                String token = tokenizerService.produceWebToken(SIGNED_ID_TOKEN_TYPE, claims.getClaimsMap());
                return new ResponseEntity<>(token, HttpStatus.OK);
            } else if (return_type == null) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (TokenizerException exception) {
            return ResponseEntity.badRequest().build();
        }

    }

    @AllowWithAuthority("popToken")
    @GetMapping("/popToken")
    public ResponseEntity<String> postPopToken(Authentication authentication) {

        if (authentication instanceof JwtAuthenticationToken<?>) {
            try {
                JwtAuthenticationToken<?> jwtAuthenticationToken = (JwtAuthenticationToken<?>) authentication;
                AuthClaims userClaims = jwtAuthenticationToken.getClaims();
                String jwkText = userClaims.getStringClaimValue(Taap.ReservedClaimNames.Confirmation);
                JsonWebKey jwkUserPublicKey = JsonWebKey.Factory.newJwk(jwkText);

                AuthClaims claims = new AuthClaimsImpl();
                claims.setGeneratedJwtId();
                claims.setIssuer(IDENTITY_ISSUER);
                claims.addAudience(IDENTITY_AUDIENCE);
                claims.setSubject(userClaims.getSubject());
                JsonWebSignature jws = new JsonWebSignature();
                jws.setPayload(claims.toJson());
                jws.setKey(jwkUserPublicKey.getKey());

                String token = jws.getCompactSerialization();

                return new ResponseEntity<>(token, HttpStatus.OK);
            } catch (JoseException | MalformedAuthClaimException exception) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}