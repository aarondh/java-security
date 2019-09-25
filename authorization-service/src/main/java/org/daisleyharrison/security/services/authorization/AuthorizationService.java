package org.daisleyharrison.security.services.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.exceptions.AuthorizationException;
import org.daisleyharrison.security.common.exceptions.AuthorizationFailedException;
import org.daisleyharrison.security.common.exceptions.AuthorizationLogoutFailedException;
import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.IdToken;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.DatastoreCursor;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.cypher.HashCypher;
import org.daisleyharrison.security.common.spi.AuthorizationServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.CypherServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.utilities.AuthClaimsImpl;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.data.models.Account;
import org.daisleyharrison.security.data.models.Activity;
import org.daisleyharrison.security.data.models.Issuer;

public final class AuthorizationService implements AuthorizationServiceProvider {
    private static Logger LOGGER = LoggerFactory.getLogger(AuthorizationService.class);
    private static final int DEFAULT_TOKEN_EXPIRY_IN_MINUTES = 120; // two hours
    private static final int DEFAULT_MAXIMUM_INVALID_LOGINS = 3;
    private static final int DEFAULT_LOCK_EXPIRY_IN_MINUTES = 120; // two hours
    private static final int DEFAULT_PASSWORD_SALT_LENGTH = 16;
    private static final String ACCOUNT_ROLE_TEST = "Test";
    private static final String ACCOUNT_ROLE_BASIC = "Basic";
    private static final LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    public enum State {
        CREATED, INITIALIZING, INITIALIZED, COMPROMISED, CLOSED, ERROR
    }

    private State state;
    private String domain;
    private String passwordSalt;
    private int maximumInvalidLogins;
    private int lockExpiryInMinutes;
    private Duration tokenExpiryInMinutes;
    private DatastoreCollection<Account> accountCollection;
    private DatastoreCollection<Activity> activityCollection;

    @Override
    public boolean isInitialized() {
        return state != State.CREATED && state != State.CLOSED && state != State.INITIALIZING;
    }

    @Override
    public boolean isReady() {
        return state == State.INITIALIZED;
    }

    private void assertReady() {
        if (!isReady()) {
            throw new IllegalStateException(
                    "authorization-service is in state " + state.toString() + " and is not ready");
        }
    }

    private void assertInitializing() {
        if (state != State.INITIALIZING) {
            throw new IllegalStateException(
                    "authorization-service is in state " + state.toString() + " and is not initializing");
        }
    }

    private static final AuthorizationServiceProvider INSTANCE = new AuthorizationService();

    public static AuthorizationServiceProvider getInstance() {
        return INSTANCE;
    }

    protected AuthorizationService() {
        this.state = State.CREATED;
    }

    private interface CloseAction {
        public void close();
    }

    private class StageImpl implements Stage {
        private CloseAction action;

        public StageImpl(CloseAction action) {
            this.action = action;
        }

        @Override
        public void close() throws IOException {
            this.action.close();
        }
    }

    public Stage beginInitialize() {
        if (this.state != State.CREATED && this.state != State.CLOSED) {
            throw new IllegalStateException(
                    "authorization-service is in state " + state.toString() + " and cannot initalize");
        }
        this.state = State.INITIALIZING;
        return new StageImpl(() -> {
            if (this.state != State.INITIALIZING) {
                throw new IllegalStateException(
                        "authorization-service is in state " + state.toString() + " and was not initializing");
            }
            if (this.accountCollection == null) {
                this.state = State.ERROR;
                throw new IllegalStateException("authorization-service did not configure correctly.");
            }
            this.state = State.INITIALIZED;
        });
    }

    private DatastoreServiceProvider getDatastore() throws ServiceNotFoundException {
        return _serviceProvider.provideService(DatastoreServiceProvider.class);
    }

    private ConfigurationServiceProvider getConfig() throws ServiceNotFoundException {
        return _serviceProvider.provideService(ConfigurationServiceProvider.class);
    }

    private CypherServiceProvider getCypherService() throws ServiceNotFoundException {
        return _serviceProvider.provideService(CypherServiceProvider.class);
    }

    private static final String TEST_ACCOUNTS_PREFIX = "authorization.test-accounts";

    private void ensureTestAccountExists(String accountId) throws ServiceNotFoundException, CypherException {
        ConfigurationServiceProvider config = getConfig();

        String accountProperty = TEST_ACCOUNTS_PREFIX + "." + accountId;

        Account account = config.getValueOfType(accountProperty, Account.class);

        String password = account.getPassword();
        if (password != null) {
            String hashedPassword = getCypherService().getHashCypher("passwordHash").hash(password.toCharArray());
            account.setHashedPassword(hashedPassword);
            LOGGER.info("test account {} password hashed to: \"{}\"", account.getEmail(), hashedPassword);
            account.setPassword(null); // This field is never used except for booting test accounts
        }

        Account existingAccount = getAccountById(accountId);

        if (existingAccount == null) {
            // This is a new account
            account.setId(accountId);
            account.setModifiedBy("system");
            account.setModified(new Date());
            account.setCreated(new Date());
            if (!account.hasRole(ACCOUNT_ROLE_TEST)) {
                account.addRole(ACCOUNT_ROLE_TEST);
            }
            this.accountCollection.insert(account);
        } else {
            // This is an existing account, just update it
            existingAccount.updateWith(account);
            if (!existingAccount.hasRole(ACCOUNT_ROLE_TEST)) {
                existingAccount.addRole(ACCOUNT_ROLE_TEST);
            }
            this.accountCollection.save(existingAccount);
        }

    }

    private void ensureAllTestAccountsAreCreated() throws ServiceNotFoundException, CypherException {
        ConfigurationServiceProvider config = getConfig();
        Set<String> accountIds = config.getNames(TEST_ACCOUNTS_PREFIX);
        for (String accountId : accountIds) {
            ensureTestAccountExists(accountId);
        }
    }

    private void removeAllDeletedTestAccounts() throws ServiceNotFoundException {
        Set<String> accountIds = getConfig().getNames(TEST_ACCOUNTS_PREFIX);
        Query all = accountCollection.buildQuery().root().property("id").isNot("").build();
        DatastoreCursor<Account> accounts = accountCollection.find(all);
        if (accountIds == null) {
            accounts.forEach(account -> {
                if (account.hasRole(ACCOUNT_ROLE_TEST)) {
                    accountCollection.remove(account);
                }
            });
        } else {
            accounts.forEach(account -> {
                if (account.hasRole(ACCOUNT_ROLE_TEST)) {
                    if (!accountIds.contains(account.getId())) {
                        accountCollection.remove(account);
                    }
                }
            });
        }
    }

    @Override
    public void configure() throws Exception {
        assertInitializing();
        try {
            ConfigurationServiceProvider config = getConfig();
            this.domain = config.getValue("authorization.domain");
            this.maximumInvalidLogins = config.getIntegerValue("authorization.maximum-invalid-logins",
                    DEFAULT_MAXIMUM_INVALID_LOGINS);
            this.lockExpiryInMinutes = config.getIntegerValue("authorization.lock-expiry-in-minutes",
                    DEFAULT_LOCK_EXPIRY_IN_MINUTES);
            int passwordSaltLength = config.getIntegerValue("authorization.password-salt-length",
                    DEFAULT_PASSWORD_SALT_LENGTH);
            this.passwordSalt = config.getValue("authorization.password-salt",
                    getCypherService().generateSalt(passwordSaltLength));
            this.tokenExpiryInMinutes = Duration.ofMinutes(
                    config.getIntegerValue("authorization.token-expiry-in-minutes", DEFAULT_TOKEN_EXPIRY_IN_MINUTES));
            this.accountCollection = getDatastore().openCollection(Account.class, "id");
            this.activityCollection = getDatastore().openCollection(Activity.class, "id");
            ensureAllTestAccountsAreCreated();
            removeAllDeletedTestAccounts();
        } catch (Exception exception) {
            LOGGER.error("authorization-service configuration failed", exception);
            this.state = State.ERROR;
            throw exception;
        }
    }

    @Override
    public String getDomain() {
        return this.domain;
    }

    private Account getAccountById(String accountId) {
        return accountCollection.findById(accountId);
    }

    private Account getAccountByEmail(String email) {
        email = email.toLowerCase();

        Query byEmail = accountCollection.buildQuery().root().property("email").is(email).build();

        Optional<Account> optAccount = accountCollection.find(byEmail).findFirst();

        if (optAccount.isEmpty()) {
            Query byIssuerEmail = accountCollection.buildQuery().root().node("issuers").property("email").is(email)
                    .build();

            optAccount = accountCollection.find(byIssuerEmail).findFirst();
        }

        if (optAccount.isPresent()) {
            return optAccount.get();
        } else {
            return null;
        }
    }

    private Account getAccountByIdToken(IdToken idToken) {
        Query byIssuerIdentifier = accountCollection.buildQuery().root().node("issuers").property("iss")
                .is(idToken.getIss()).and("sub").is(idToken.getSub()).build();

        Optional<Account> optAccount = accountCollection.find(byIssuerIdentifier).findFirst();

        if (optAccount.isPresent()) {
            return optAccount.get();
        } else {
            return null;
        }
    }

    @Override
    public String getIssuerForEmail(String email) {
        Account account = getAccountByEmail(email);
        if (account != null) {
            List<Issuer> issuers = account.getIssuers();
            if (issuers.size() == 0) {
                return this.domain;
            } else {
                for (Issuer issuer : issuers) {
                    if (issuer.isPrimary()) {
                        return issuer.getName();
                    }
                }
                return issuers.iterator().next().getName();
            }

        }
        return null;
    }

    private AuthClaims toAuthClaims(Account account, String issuer, IdToken idToken, String... requestedScopes)
            throws MalformedAuthClaimException {
        AuthClaims claims = new AuthClaimsImpl();

        claims.setIssuer(getDomain());
        claims.addAudience(getDomain());
        claims.setSubject(account.getId());
        claims.setGivenName(account.getGivenName());
        claims.setMiddleName(account.getMiddleName());
        claims.setFamilyName(account.getFamilyName());
        StringBuilder name = new StringBuilder();
        if (account.getGivenName() == null) {
            name.append(account.getFamilyName());
        } else if (account.getFamilyName() == null) {
            name.append(account.getGivenName());
        } else {
            name.append(account.getGivenName());
            if (account.getMiddleName() != null) {
                name.append(" ");
                name.append(account.getMiddleName());
            }
            name.append(", ");
            name.append(account.getFamilyName());
        }
        claims.setName(name.toString());

        String preferredUsername = account.getPreferredUsername();
        if (preferredUsername == null) {
            preferredUsername = account.getGivenName();
            if (preferredUsername == null) {
                preferredUsername = account.getFamilyName();
            }
        }
        claims.setPreferredUsername(preferredUsername);

        claims.setEmail(account.getEmail());
        claims.setClaim("phn", account.getPhoneNumber());
        claims.setPicture(account.getPictureUrl());

        for (String scope : requestedScopes) {
            if (account.hasRole(scope)) {
                claims.addScope(scope);
            }
        }

        claims.setClaim(AuthClaims.PrivateClaims.ID_TOKEN_ISSUER, issuer);
        Date expires = Date.from(new Date().toInstant().plus(this.tokenExpiryInMinutes));
        claims.setExpirationTime(expires);

        AuthClaims id_token = new AuthClaimsImpl();
        if (idToken == null) {
            id_token.setIssuer(getDomain());
            id_token.addAudience(getDomain());
            id_token.setSubject(account.getId());
            id_token.setExpirationTime(expires);
        } else {
            id_token.setIssuer(idToken.getIss());
            id_token.addAudience(idToken.getAud());
            id_token.setSubject(idToken.getSub());
            id_token.setExpirationTime(idToken.getExp());
            id_token.setClaim("iat", idToken.getIat());
        }
        claims.setClaim(AuthClaims.PrivateClaims.ID_TOKEN, id_token);

        return claims;
    }

    private AuthClaims loginInternal(Account account, String how) throws AuthorizationException {
        if (account == null) {
            throw new AuthorizationFailedException();
        } else if (account.isBlocked()) {
            activityCollection
                    .insert(new Activity(getDomain(), account.getId(), "Attemping to access a blocked account"));
            throw new AuthorizationFailedException("Blocked: " + account.getBlockReason());
        } else if (account.isLocked()) {
            activityCollection
                    .insert(new Activity(getDomain(), account.getId(), "Attemping to access a locked account"));
            throw new AuthorizationFailedException("Locked: " + account.getLockReason());
        }

        account.setLastLogin(new Date());
        account.setConcurentLogins(account.getConcurentLogins() + 1);
        account.setInvalidLogins(0);
        accountCollection.save(account);

        activityCollection.insert(new Activity(getDomain(), account.getId(), "Login " + how));

        return toAuthClaims(account, getDomain(), null, ACCOUNT_ROLE_BASIC);
    }

    private String toString(Duration duration) {
        return String.format("%d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    @Override
    public AuthClaims login(String email, char[] password) throws AuthorizationException {
        assertReady();
        Account account = getAccountByEmail(email);
        if (account == null) {
            throw new AuthorizationFailedException();
        } else if (account.isBlocked()) {
            activityCollection
                    .insert(new Activity(getDomain(), account.getId(), "Attemping to access a blocked account"));
            throw new AuthorizationFailedException("Blocked: " + account.getBlockReason());
        } else if (account.isLocked()) {
            Duration lockedFor = Duration.between(account.getLockDate().toInstant(), new Date().toInstant());
            if (lockedFor.toMinutes() > this.lockExpiryInMinutes) {
                String activity = String.format("Account unlocked after %d minutes", this.lockExpiryInMinutes);
                activityCollection.insert(new Activity(getDomain(), account.getId(), activity));
            } else {
                String activity = String.format("Attempt to access locked account (lock %s)", toString(lockedFor));
                activityCollection.insert(new Activity(getDomain(), account.getId(), activity));
                throw new AuthorizationFailedException("Locked: " + account.getLockReason());
            }
        }
        try {
            HashCypher hashCypher = _serviceProvider.provideService(CypherServiceProvider.class).getHashCypher("passwordHash");

            String hashedPassword = account.getHashedPassword();
            if (hashedPassword != null && hashCypher.verify(hashedPassword, password)) {
                return loginInternal(account, "locally");
            } else {
                int invalidLogins = account.getInvalidLogins() + 1;
                if (invalidLogins >= this.maximumInvalidLogins) {
                    account.setLockReason("Too many invalid logins");
                    account.setLockDate(new Date());
                    String activity = String.format("Maximum (%d) invalid login attempted. Account locked.",
                            this.maximumInvalidLogins);
                    activityCollection.insert(new Activity(getDomain(), account.getId(), activity));
                } else {
                    account.setInvalidLogins(invalidLogins);
                    String activity = String.format("Invalid login attempt %d of %d", invalidLogins,
                            this.maximumInvalidLogins);
                    activityCollection.insert(new Activity(getDomain(), account.getId(), activity));
                }
                accountCollection.save(account);
                throw new AuthorizationFailedException();
            }
        } catch (ServiceNotFoundException | CypherException exception) {
            throw new AuthorizationFailedException();
        }
    }

    @Override
    public AuthClaims loginByUserClaims(String issuer, AuthClaims userClaims) throws AuthorizationException {
        assertReady();
        IdToken idToken = new IdToken(userClaims);
        Account account = getAccountByIdToken(idToken);

        return loginInternal(account, "using issuer " + issuer);
    }

    @Override
    public AuthClaims createAccount(AuthClaims newUserClaims) throws AuthorizationException {
        assertReady();
        Account account = Account.newAccount("newUser");
        account.setModifiedBy(account.getId());
        account.setGivenName(newUserClaims.getGivenName());
        account.setFamilyName(newUserClaims.getFamilyName());
        account.setPreferredUsername(newUserClaims.getPreferredUsername());
        account.setEmail(newUserClaims.getEmail());
        Issuer issuer = new Issuer();
        issuer.setName(newUserClaims.getIdTokenIssuer());
        AuthClaims id_token_claims = (AuthClaims) newUserClaims.getIdToken();
        IdToken idToken = new IdToken(id_token_claims);
        issuer.setIss(idToken.getIss());
        issuer.setSub(idToken.getSub());
        issuer.setPrimary(true);
        issuer.setEmail(account.getEmail());
        account.addIssuer(issuer);
        account.addRole(ACCOUNT_ROLE_BASIC);
        accountCollection.insert(account);

        activityCollection.insert(new Activity(getDomain(), account.getId(), "Account created"));

        return toAuthClaims(account, issuer.getName(), idToken, ACCOUNT_ROLE_BASIC);
    }

    @Override
    public void deleteAccount(String accoundId) {
        assertReady();
        throw new IllegalStateException();
    }

    @Override
    public void logout(AuthClaims userClaims) throws AuthorizationException {
        assertReady();
        if (userClaims.getAudience().contains(getDomain())) {
            Account account = getAccountById(userClaims.getSubject());
            if (account == null) {
                throw new AuthorizationLogoutFailedException("Cannot logout. Account not found.");
            } else {
                if (account.getConcurentLogins() > 0) {
                    account.setConcurentLogins(account.getConcurentLogins() - 1);
                    accountCollection.save(account);
                }
                String sessionDurationText;
                if (account.getLastLogin() == null) {
                    sessionDurationText = "unknown";
                } else {
                    Duration sessionDuration = Duration.between(account.getLastLogin().toInstant(),
                            new Date().toInstant());
                    sessionDurationText = toString(sessionDuration);
                }
                String activity = String.format("Logout (duration %s)", sessionDurationText);

                activityCollection.insert(new Activity(getDomain(), account.getId(), activity));
            }
        } else {
            throw new AuthorizationLogoutFailedException(
                    "Cannot logout.  User claims not issued by " + getDomain() + " domain");
        }
    }

    @Override
    public void close() {
        this.state = State.CLOSED;
    }
}