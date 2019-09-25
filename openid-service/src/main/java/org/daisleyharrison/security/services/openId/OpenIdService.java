package org.daisleyharrison.security.services.openId;

import org.daisleyharrison.security.common.spi.OpenIdServiceProvider;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;

import org.daisleyharrison.security.services.openId.utilities.StringUtils;
import org.daisleyharrison.security.common.models.openId.OpenIdServiceConfig;
import org.daisleyharrison.security.common.models.openId.OpenIdState;
import org.daisleyharrison.security.common.models.openId.OpenIdTokenType;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.openId.CompletableFutureBuilder;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.openId.OpenIdDiscovery;
import org.daisleyharrison.security.common.models.openId.OpenIdHeaders;
import org.daisleyharrison.security.common.models.openId.OpenIdPropertyDef;
import org.daisleyharrison.security.common.models.openId.OpenIdResponse;
import org.daisleyharrison.security.common.models.openId.OpenIdIssuer;
import org.daisleyharrison.security.common.exceptions.OpenIdException;
import org.daisleyharrison.security.common.utilities.AuthClaimsImpl;
import org.daisleyharrison.security.common.utilities.SecureRandomUtil;
import org.daisleyharrison.security.services.openId.utilities.OpenIdPropertyDefUtils;
import org.daisleyharrison.security.services.openId.utilities.URIBuilder;
import org.daisleyharrison.security.services.openId.utilities.Signal;

public final class OpenIdService implements OpenIdServiceProvider {

	private static Logger LOGGER = LoggerFactory.getLogger(OpenIdService.class);

	private static ObjectMapper objectMapper = new ObjectMapper();

	private OpenIdServiceConfig configuration;

	private String domain;

	private boolean debug;

	private ServiceState state;

	private enum IssuerState {
		UNDISCOVERED, DISCOVERING, DISCOVERED, ERROR
	}

	private enum ServiceState {
		CREATED, INITIALIZING, INITIALIZED, COMPROMIZED, ERROR, CLOSED
	}

	private static final int DEFAULT_NONCE_LENGTH = 8;

	// Well known openid parameter names
	private static final String PROPERTY_NONCE = "nonce";

	private static final String PROPERTY_STATE = "state";

	private static final String PROPERTY_CLIENT_ID = "client_id";

	private static final String PROPERTY_CLIENT_SECRET = "client_secret";

	private class IssuerDefinition implements Closeable {
		private String issuerName;
		private OpenIdDiscovery discovery;
		private Signal discovered;
		private IssuerState state;
		private OpenIdIssuer config;
		private String clientId;
		private char[] clientSecret;
		private Throwable exception;

		public IssuerDefinition(OpenIdIssuer config) throws CloneNotSupportedException {
			this.config = (OpenIdIssuer) config.clone();
			this.issuerName = this.config.getIssuer();
			this.discovered = new Signal();
			this.state = IssuerState.UNDISCOVERED;
			if (this.config.getDiscoveryEndpoint() == null) {
				setDiscovery(this.config.getConfiguration());
			}
		}

		public OpenIdIssuer getConfig() {
			return this.config;
		}

		public boolean isDiscoverable() {
			return this.config.getDiscoveryEndpoint() != null;
		}

		public void setState(IssuerState state) {
			this.state = state;
		}

		public IssuerState getState() {
			return this.state;
		}

		public String getIssuerName() {
			return this.issuerName;
		}

		public boolean isDiscovered() {
			return this.discovered.isSignalled();
		}

		public OpenIdDiscovery getDiscovery() {
			return this.discovery;
		}

		public Duration getDiscoveryTimeout() {
			return Duration.ofMinutes(1);
		}

		public Duration getTokenTimeout() {
			return Duration.ofMinutes(1);
		}

		public OpenIdDiscovery waitForDiscovery() throws InterruptedException, Throwable {
			this.discovered.waitForSignal();
			if (this.discovery == null && this.exception != null) {
				Throwable throwable = getException();
				clearException();
				throw throwable;
			}
			return this.discovery;
		}

		public void markDiscovering() {
			setState(IssuerState.DISCOVERING);
			this.discovered.clearSignal();
		}

		public Throwable getException() {
			return this.exception;
		}

		public void clearException() {
			if (this.exception != null) {
				this.exception = null;
				if (this.discovery == null) {
					setState(IssuerState.UNDISCOVERED);
				} else {
					setState(IssuerState.DISCOVERED);
				}
			}
		}

		public void abortDiscovery(Throwable reason) {
			this.exception = reason;
			setState(IssuerState.ERROR);
			this.discovery = null;
			this.discovered.setSignal();
		}

		public void setDiscovery(OpenIdDiscovery discovery) {
			this.discovery = discovery;
			setState(IssuerState.DISCOVERED);
			this.discovered.setSignal();
		}

		public String getClientId() {
			return this.clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public char[] getClientSecret() {
			return this.clientSecret;
		}

		public void setClientSecret(char[] clientSecret) {
			this.clientSecret = clientSecret;
		}

		@Override
		public void close() {
			this.discovered.abortSignal();
		}

	}

	private interface DoBuild<T> {
		CompletableFuture<T> Action(Set<OpenIdProperty> properties) throws OpenIdException;
	}

	public class CompletableFutureBuilderImpl<T> implements CompletableFutureBuilder<T> {
		private OpenIdService service;
		private IssuerDefinition issuer;
		private OpenIdPropertyDef.Usage usage;
		private Map<String, OpenIdPropertyDef> propertyDefs;
		private Map<String, OpenIdProperty> properties;
		private DoBuild<T> action;
		private String state;
		private boolean isAllowCustomProperties;

		private CompletableFutureBuilderImpl(OpenIdService service, IssuerDefinition issuer,
				OpenIdPropertyDef.Usage usage, DoBuild<T> action) {
			this.service = service;
			this.issuer = issuer;
			this.usage = usage;
			this.action = action;
			this.propertyDefs = new HashMap<String, OpenIdPropertyDef>();
			this.properties = new HashMap<String, OpenIdProperty>();
			this.isAllowCustomProperties = false;
			for (OpenIdPropertyDef definition : issuer.getConfig().getProperties()) {
				if (definition.getUsage().equals(usage)) {
					this.propertyDefs.put(definition.getName(), definition);
					String default_value = definition.getDefaultValue();
					if (default_value != null) {
						this.properties.put(definition.getName(), new OpenIdProperty(definition));
					}
				}
			}
		}

		private CompletableFutureBuilderImpl(OpenIdService service, DoBuild<T> action) {
			this.service = service;
			this.action = action;
			this.propertyDefs = new HashMap<String, OpenIdPropertyDef>();
			this.properties = new HashMap<String, OpenIdProperty>();
			this.isAllowCustomProperties = true;
		}

		private OpenIdPropertyDef findPropertyDef(String propertyName) {
			if (propertyName == null) {
				throw new IllegalArgumentException("propertyName cannot be null");
			}
			return this.propertyDefs.get(propertyName);
		}

		private OpenIdPropertyDef getPropertyDef(String propertyName) {
			OpenIdPropertyDef definition = findPropertyDef(propertyName);
			if (definition == null) {
				throw new IllegalArgumentException(
						"property \"" + propertyName + "\" is not defined for " + this.usage + "\" on this issuer.");
			}
			return definition;
		}

		@Override
		public CompletableFutureBuilder<T> setProperty(String propertyName, String propertyValue) {
			OpenIdPropertyDef definition;
			if (this.isAllowCustomProperties) {
				definition = findPropertyDef(propertyName);
				if (definition == null) {
					definition = new OpenIdPropertyDef(OpenIdPropertyDef.Usage.CUSTOM, OpenIdPropertyDef.RestPart.BODY,
							propertyName, null);
				}
			} else {
				definition = getPropertyDef(propertyName);
			}
			this.properties.put(definition.getName(), new OpenIdProperty(definition, propertyValue));
			return this;
		}

		@Override
		public CompletableFutureBuilder<T> setProperties(Map<String, String> properties) {
			if (properties == null) {
				throw new IllegalArgumentException("properties cannot be null");
			}
			properties.forEach((propertyName, propertyValue) -> {
				setProperty(propertyName, propertyValue);
			});
			return this;
		}

		@Override
		public Map<String, String> getProperties() {
			Map<String, String> properties = new HashMap<>();
			this.properties.forEach((name, property) -> {
				properties.put(name, property.getValue());
			});
			return properties;
		}

		@Override
		public String getProperty(String propertyName) {
			OpenIdProperty property = this.properties.get(propertyName);
			if (property == null) {
				OpenIdPropertyDef propertyDef = getPropertyDef(propertyName);
				return propertyDef.getDefaultValue();
			}
			return property.getValue();
		}

		@Override
		public CompletableFutureBuilder<T> setState(String state) {
			this.state = state;
			return this;
		}

		@Override
		public String getState() {
			return this.state;
		}

		private void addIfRequired(Map<String, OpenIdProperty> properties, String propertyName,
				Supplier<String> valueSupplier) {
			OpenIdPropertyDef definition = findPropertyDef(propertyName);
			if (definition != null) {
				String value = valueSupplier.get();
				if (value != null) {
					properties.put(propertyName, new OpenIdProperty(definition, value));
				}
			}
		}

		private void addCharsIfRequired(Map<String, OpenIdProperty> properties, String propertyName,
				Supplier<char[]> valueSupplier) {
			OpenIdPropertyDef definition = findPropertyDef(propertyName);
			if (definition != null) {
				char[] value = valueSupplier.get();
				if (value != null) {
					properties.put(propertyName, new OpenIdProperty(definition, value));
				}
			}
		}

		private Set<OpenIdProperty> completeProperties(Map<String, OpenIdProperty> properties) {
			Map<String, OpenIdProperty> extendedProperties = new HashMap<>(properties);

			addIfRequired(extendedProperties, PROPERTY_NONCE, () -> {
				return SecureRandomUtil.generateRandomString(DEFAULT_NONCE_LENGTH);
			});

			addIfRequired(extendedProperties, PROPERTY_STATE, () -> {
				String issuerName = this.issuer.getIssuerName();
				String clientId = this.issuer.getClientId();
				try {
					return this.service.createStateToken(issuerName, clientId, getState(), extendedProperties);
				} catch (JoseException exception) {
					return "INVALID-STATE";
				}
			});
			addIfRequired(extendedProperties, PROPERTY_CLIENT_ID, () -> this.issuer.getClientId());
			addCharsIfRequired(extendedProperties, PROPERTY_CLIENT_SECRET, () -> this.issuer.getClientSecret());

			// find any missing but required properties
			Set<OpenIdPropertyDef> missing = new HashSet<>();
			this.propertyDefs.forEach((name, definition) -> {
				if (definition.isRequired() && !extendedProperties.containsKey(name)) {
					missing.add(definition);
				}
			});
			if (!missing.isEmpty()) {
				throw new IllegalArgumentException(OpenIdPropertyDefUtils.createRequiredMissingMessage(missing));
			}

			// Create the final set of properties
			Set<OpenIdProperty> finalProperties = new HashSet<>();
			extendedProperties.forEach((key, value) -> {
				finalProperties.add(value);
			});
			if (this.service.isDebug()) {
				StringBuilder propertiesMessage = new StringBuilder();
				finalProperties.forEach((property) -> {
					if (propertiesMessage.length() != 0) {
						propertiesMessage.append(", ");
					}
					propertiesMessage.append(String.format("%s@%s = \"%s\"", property.getName(), property.getRestPart(),
							property.getValue()));
				});
				LOGGER.info(propertiesMessage.toString());
			}

			return finalProperties;
		}

		@Override
		public CompletableFuture<T> build() throws OpenIdException {
			return this.action.Action(completeProperties(this.properties));
		}
	}

	private Map<String, IssuerDefinition> issuers = new HashMap<>();

	public OpenIdService() {
		this.state = ServiceState.CREATED;
	}

	@Override
	public boolean isReady() {
		return this.state == ServiceState.INITIALIZED;
	}

	@Override
	public boolean isInitialized() {
		return this.state != ServiceState.CREATED && this.state != ServiceState.INITIALIZING;
	}
	@Override
	public void setConfiguration(OpenIdServiceConfig configuration){
		assertInitializing();
		if (configuration == null) {
			throw new IllegalArgumentException("configuration cannot be null");
		}
		this.configuration = configuration;
	}
	@Override
	public void configure() throws Exception {
		assertInitializing();
		if (this.configuration == null) {
			throw new IllegalArgumentException("openid-service configuration not set (call setConfiguration() first)");
		}
		try {
			this.configuration = (OpenIdServiceConfig) configuration.clone();
		} catch (CloneNotSupportedException exception) {
			throw new OpenIdException(exception);
		}
		if (this.configuration.getIssuers() == null || this.configuration.getIssuers().length == 0) {
			throw new IllegalStateException("no issuers defined in the configuration");
		}
		OpenIdPropertyDef[] configProperties = this.configuration.getProperties();
		for (OpenIdIssuer openidIssuer : this.configuration.getIssuers()) {
			if (openidIssuer.getDiscoveryEndpoint() == null && openidIssuer.getConfiguration() == null) {
				throw new IllegalArgumentException("Issuer \"" + openidIssuer.getIssuer()
						+ "\" does not define a configuration or a discovery endpoint");
			}
			openidIssuer
					.setProperties(OpenIdPropertyDefUtils.merge(openidIssuer.getProperties(), configProperties, true));
			try {
				IssuerDefinition issuer = new IssuerDefinition(openidIssuer);
				this.issuers.put(issuer.getIssuerName(), issuer);
			} catch (CloneNotSupportedException exception) {
				throw new OpenIdException(exception);
			}

		}
	}

	private void assertReady() {
		if (!isReady()) {
			throw new IllegalStateException("openid-service is in state " + state.toString() + " and is not ready");
		}
	}

	private void assertInitializing() {
		if (this.state != ServiceState.INITIALIZING) {
			throw new IllegalStateException(
					"openid-service is in state " + state.toString() + " and is not initializing");
		}
	}

	private interface CloseAction {
		public void close();
	}

	private class StageImpl implements Stage {
		private CloseAction closeAction;

		public StageImpl(CloseAction closeAction) {
			this.closeAction = closeAction;
		}

		@Override
		public void close() throws IOException {
			this.closeAction.close();
		}

	}

	@Override
	public Stage beginInitialize() {
		if (this.state != ServiceState.CREATED) {
			throw new IllegalStateException("openid-service cannot be initialized");
		}
		this.state = ServiceState.INITIALIZING;
		StageImpl stage = new StageImpl(() -> {
			if (this.state != ServiceState.INITIALIZING) {
				throw new IllegalStateException("openid-service was not initiailizing");
			}
			this.state = ServiceState.INITIALIZED;
		});
		return stage;
	}

	@Override
	public void setDomain(String domain) {
		if (domain == null) {
			throw new IllegalArgumentException("domain cannot be null");
		}
		assertInitializing();
		if (this.domain != null) {
			throw new IllegalStateException("domain was aleady set");
		}
		this.domain = domain;
	}

	@Override
	public String getDomain() {
		return this.domain;
	}

	@Override
	public void setDebug(boolean debug) {
		assertInitializing();
		this.debug = debug;
	}

	@Override
	public boolean isDebug() {
		return this.debug;
	}

	private IssuerDefinition getIssuer(String issuerName) {
		if (issuerName == null) {
			throw new IllegalArgumentException("issuerName cannot be null");
		}
		IssuerDefinition issuer = this.issuers.get(issuerName);
		if (issuer == null) {
			throw new IllegalArgumentException("Unsupported issuer \"" + issuerName + "\"");
		}
		return issuer;
	}

	@Override
	public void setClientCredentials(String issuerName, String clientId, char[] clientSecret) {
		assertInitializing();
		IssuerDefinition issuer = getIssuer(issuerName);
		issuer.setClientId(clientId);
		issuer.setClientSecret(clientSecret);

	}

	// TBD: This is a test key only (DO NOT USE IN FINAL)
	private static final String JWK_STATE_KEY_JSON = "{\"kty\":\"oct\",\"k\":\"Fdh9u8rINxfivbrianbbVT1u232VQBZYKx1HGAGPt2I\"}";

	private static final String STATE_TOKEN_CLAIM_OPENID_PREFIX = "toi::";
	private static final String STATE_TOKEN_SUBJECT = "TOI::STATE";
	private static final String STATE_TOKEN_CLAIM_OPENID_ISSUER = STATE_TOKEN_CLAIM_OPENID_PREFIX + "iss";
	private static final String STATE_TOKEN_CLAIM_OPENID_CLIENT_ID = STATE_TOKEN_CLAIM_OPENID_PREFIX + "cid";
	private static final String STATE_TOKEN_CLAIM_OPENID_USER_STATE = STATE_TOKEN_CLAIM_OPENID_PREFIX + "stt";
	private static final String BEARER_TOKEN_SUBJECT = "TOI::BEARER";

	private JsonWebKey getStateTokenKey() throws JoseException {
		return JsonWebKey.Factory.newJwk(JWK_STATE_KEY_JSON);
	}

	private JsonWebKey getBearerTokenKey() throws JoseException {
		return JsonWebKey.Factory.newJwk(JWK_STATE_KEY_JSON);
	}

	private String createStateToken(String issuer, String clientId, String userState,
			Map<String, OpenIdProperty> properties) throws JoseException {

		// Create the set of claims for the state token

		JwtClaims claims = new JwtClaims();
		claims.setIssuer(getDomain());
		claims.setAudience(getDomain());
		claims.setExpirationTimeMinutesInTheFuture(10);
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();
		claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
		claims.setSubject(STATE_TOKEN_SUBJECT);
		claims.setClaim(STATE_TOKEN_CLAIM_OPENID_ISSUER, issuer);
		claims.setClaim(STATE_TOKEN_CLAIM_OPENID_CLIENT_ID, clientId);

		properties.forEach((name, property) -> {
			String value = property.getValue();
			if (value != null && !value.isEmpty()) {
				claims.setClaim(STATE_TOKEN_CLAIM_OPENID_PREFIX + name, value);
			}
		});

		if (userState != null && !userState.isBlank()) {
			claims.setClaim(STATE_TOKEN_CLAIM_OPENID_USER_STATE, userState);
		}

		JsonWebEncryption jwe = new JsonWebEncryption();

		jwe.setContentTypeHeaderValue("JWT");

		jwe.setPlaintext(claims.toJson());

		jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);

		jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);

		jwe.setKey(getStateTokenKey().getKey());

		String stateToken = jwe.getCompactSerialization();

		return stateToken;
	}

	private JwtClaims validateStateTokenInternal(String stateToken) throws OpenIdException {
		try {
			JsonWebEncryption jwe = new JsonWebEncryption();

			AlgorithmConstraints algConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST,
					KeyManagementAlgorithmIdentifiers.DIRECT);
			jwe.setAlgorithmConstraints(algConstraints);

			AlgorithmConstraints encConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST,
					ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
			jwe.setContentEncryptionAlgorithmConstraints(encConstraints);

			// Set the compact serialization on new Json Web Encryption object
			jwe.setCompactSerialization(stateToken);

			jwe.setKey(getStateTokenKey().getKey());

			// Decrypt the JWE
			String stateJson = jwe.getPlaintextString();
			JwtClaims jwtClaims = JwtClaims.parse(stateJson);
			if (jwtClaims.getIssuer() == null || !jwtClaims.getIssuer().equals(getDomain())) {
				throw new OpenIdException("Invalid state token: issuer not valid");
			}
			boolean audienceFound = false;
			if (jwtClaims.hasAudience()) {
				for (String audience : jwtClaims.getAudience()) {
					if (audience.equals(getDomain())) {
						audienceFound = true;
						break;
					}
				}
			}
			if (!audienceFound) {
				throw new OpenIdException("Invalid state token: audience not valid");
			}
			if (!STATE_TOKEN_SUBJECT.equals(jwtClaims.getSubject())) {
				throw new OpenIdException("Invalid state token: subject not valid");
			}
			return jwtClaims;
		} catch (JoseException | InvalidJwtException | MalformedClaimException exception) {
			throw new OpenIdException("Invalid state token", exception);
		}
	}

	private class OpenIdStateImpl implements OpenIdState {
		private String issuer;
		private String state;

		public OpenIdStateImpl(String issuer, String state) {
			this.issuer = issuer;
			this.state = state;
		}

		@Override
		public String getIssuer() {
			return this.issuer;
		}

		@Override
		public String getState() {
			return this.state;
		}

	}

	@Override
	public CompletableFuture<OpenIdState> validateStateToken(String token) throws OpenIdException {
		assertReady();
		try {
			JwtClaims claims = validateStateTokenInternal(token);

			OpenIdState openidState = new OpenIdStateImpl(
					claims.getClaimValue(STATE_TOKEN_CLAIM_OPENID_ISSUER, String.class),
					claims.getClaimValue(STATE_TOKEN_CLAIM_OPENID_USER_STATE, String.class));

			return CompletableFuture.completedFuture(openidState);
		} catch (OpenIdException exception) {
			throw new OpenIdException("Invalid state token", exception.getCause());
		} catch (MalformedClaimException exception) {
			throw new OpenIdException("Invalid state token", exception);
		}
	}

	@Override
	public CompletableFutureBuilder<String> createStateToken(String issuerName) throws OpenIdException {
		assertReady();
		IssuerDefinition issuer = getIssuer(issuerName);
		return new CompletableFutureBuilderImpl<String>(this, issuer, OpenIdPropertyDef.Usage.AUTHENTICATION,
				(properties) -> {
					for (OpenIdProperty property : properties) {
						if (property.getName().equals(PROPERTY_STATE)) {
							return CompletableFuture.completedFuture(property.getValue());
						}
					}
					return null;
				});
	}

	private CompletableFuture<OpenIdDiscovery> doDiscovery(IssuerDefinition issuer, Set<OpenIdProperty> properties)
			throws OpenIdException {
		if (issuer == null) {
			throw new IllegalArgumentException("issuer cannot be null");
		}
		if (properties == null) {
			throw new IllegalArgumentException("properties cannot be null");
		}
		switch (issuer.getState()) {
		case ERROR:
			Throwable throwable = issuer.getException();
			issuer.clearException();
			throw new OpenIdException(throwable);
		case DISCOVERED:
			return CompletableFuture.completedFuture(issuer.getDiscovery());
		case DISCOVERING:
			return CompletableFuture.supplyAsync(new Supplier<OpenIdDiscovery>() {
				@Override
				public OpenIdDiscovery get() {
					try {
						issuer.waitForDiscovery();
						return issuer.getDiscovery();
					} catch (Throwable exception) {
						throw new CompletionException(exception);
					}
				}
			});
		case UNDISCOVERED:
			try {
				Map<String, String> uriProperties = OpenIdProperty.propertyMapFor(OpenIdPropertyDef.RestPart.URI,
						properties);
				Map<String, String> queryParameters = OpenIdProperty.propertyMapFor(OpenIdPropertyDef.RestPart.QUERY,
						properties);
				URI discoveryUri = new URIBuilder(issuer.getConfig().getDiscoveryEndpoint())
						.setProperties(uriProperties).setParameters(queryParameters).build();
				LOGGER.debug("retrieving discovery document for issuer {} from {}", issuer.getIssuerName(),
						discoveryUri);

				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder().uri(discoveryUri).timeout(issuer.getDiscoveryTimeout())
						.build();

				issuer.markDiscovering();
				return client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
						.thenApply((String jsonText) -> {
							try {
								OpenIdDiscovery discovery = objectMapper.readValue(jsonText, OpenIdDiscovery.class);
								issuer.setDiscovery(discovery);
								LOGGER.info("discovery document retrieved for issuer {}", issuer.getIssuerName());
								return discovery;
							} catch (IOException exception) {
								issuer.abortDiscovery(exception);
								LOGGER.error("invalid discovery document retrieved for issuer {}: {}",
										issuer.getIssuerName(), StringUtils.toElipsis(jsonText));
								throw new CompletionException(
										String.format("Invalid discovery documentt retrieved for issuer %s: \"%s\"",
												issuer.getIssuerName(), StringUtils.toElipsis(jsonText)),
										exception);
							}
						});
			} catch (URISyntaxException | IOException exception) {
				throw new CompletionException(exception);
			}
		default:
			throw new OpenIdException("Issuer is in an imcompatable state");
		}

	}

	private CompletableFutureBuilder<OpenIdDiscovery> getDiscovery(IssuerDefinition issuer) throws OpenIdException {
		return new CompletableFutureBuilderImpl<OpenIdDiscovery>(this, issuer, OpenIdPropertyDef.Usage.DISCOVERY,
				(properties) -> {
					return doDiscovery(issuer, properties);
				});
	}

	@Override
	public CompletableFutureBuilder<Boolean> discover(String issuerName) throws OpenIdException {
		assertReady();
		IssuerDefinition issuer = getIssuer(issuerName);
		return new CompletableFutureBuilderImpl<Boolean>(this, issuer, OpenIdPropertyDef.Usage.DISCOVERY,
				(properties) -> {
					return doDiscovery(issuer, properties).thenApply(discovery -> discovery != null);
				});
	}

	@Override
	public CompletableFuture<Boolean> supportsEndSession(String issuerName) throws OpenIdException {
		assertReady();
		IssuerDefinition issuer = getIssuer(issuerName);
		return getDiscovery(issuer).build().thenApply(discovery -> discovery.isFrontChannelLogoutSupported());
	}

	@Override
	public CompletableFutureBuilder<URI> getAuthenticationURI(String issuerName)
			throws OpenIdException, URISyntaxException {
		assertReady();
		IssuerDefinition issuer = getIssuer(issuerName);
		return new CompletableFutureBuilderImpl<URI>(this, issuer, OpenIdPropertyDef.Usage.AUTHENTICATION,
				(properties) -> {
					return getDiscovery(issuer).build().thenApplyAsync(discovery -> {
						try {
							Map<String, String> uriProperties = OpenIdProperty
									.propertyMapFor(OpenIdPropertyDef.RestPart.URI, properties);
							Map<String, String> queryProperties = OpenIdProperty
									.propertyMapFor(OpenIdPropertyDef.RestPart.QUERY, properties);
							URIBuilder uriBuilder = new URIBuilder(discovery.getAuthorizationEndpoint())
									.setProperties(uriProperties).setParameters(queryProperties);
							return uriBuilder.build();
						} catch (URISyntaxException | UnsupportedEncodingException exception) {
							throw new CompletionException(exception);
						}
					});
				});
	}

	@Override
	public CompletableFutureBuilder<URI> getEndSessionURI(String issuerName)
			throws OpenIdException, URISyntaxException {
		assertReady();
		IssuerDefinition issuer = getIssuer(issuerName);
		return new CompletableFutureBuilderImpl<URI>(this, issuer, OpenIdPropertyDef.Usage.AUTHENTICATION,
				(properties) -> {
					return getDiscovery(issuer).build().thenApplyAsync(discovery -> {
						try {
							Map<String, String> uriProperties = OpenIdProperty
									.propertyMapFor(OpenIdPropertyDef.RestPart.URI, properties);
							Map<String, String> queryProperties = OpenIdProperty
									.propertyMapFor(OpenIdPropertyDef.RestPart.QUERY, properties);
							URIBuilder uriBuilder = new URIBuilder(discovery.getEndSessionEndpoint())
									.setProperties(uriProperties).setParameters(queryProperties);
							return uriBuilder.build();
						} catch (URISyntaxException | UnsupportedEncodingException exception) {
							throw new CompletionException(exception);
						}
					});
				});
	}

	/**
	 * Method to extract token based on tokenType from json string. Before
	 * returning, it will perform validation of the token by signature verification
	 * using OpenId server keys
	 * 
	 * @param jsonString
	 * @param tokenType
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	private static AuthClaims parseToken(IssuerDefinition issuer, String jsonString, String clientId,
			OpenIdTokenType tokenType)
			throws IOException, JoseException, InvalidJwtException, JsonParseException, OpenIdException {

		JsonFactory jsonFactory = new JsonFactory();
		JsonParser parser = jsonFactory.createParser(jsonString);
		String token = null;
		boolean isError = false;
		String errorName = "";
		String errorDescription = "";
		// Extracting token from json string by parsing it
		while (!parser.isClosed()) {
			JsonToken nextToken = parser.nextToken();
			if (JsonToken.FIELD_NAME.equals(nextToken)) {
				String fieldName = parser.getCurrentName();
				if (fieldName.equals(tokenType.getTokenType())) {
					nextToken = parser.nextToken();
					token = parser.getValueAsString();
					break;
				} else if (fieldName == "error") {
					isError = true;
					nextToken = parser.nextToken();
					errorName = parser.getValueAsString();
				} else if (isError && fieldName == "error_description") {
					nextToken = parser.nextToken();
					errorDescription = parser.getValueAsString();
				}
			}
		}
		if (isError) {
			throw new OpenIdException(errorName + ": " + errorDescription);
		} else if (token == null) {
			return null;
		} else {
			// Setting OpenId server keys to verify signature of OpenId token
			HttpsJwks jwks = new HttpsJwks(issuer.getDiscovery().getJwksUri());
			List<JsonWebKey> jwksList = jwks.getJsonWebKeys();
			JwksVerificationKeyResolver keys = new JwksVerificationKeyResolver(jwksList);
			JwtConsumerBuilder consumerBuilder = new JwtConsumerBuilder().setVerificationKeyResolver(keys);
			if (OpenIdTokenType.ID_TOKEN.equals(tokenType)) {
				consumerBuilder.setExpectedAudience(clientId).build(); // Setting audience as client ID
			}
			JwtConsumer consumer = consumerBuilder.build();
			JwtClaims claims = consumer.processToClaims(token);
			return new AuthClaimsImpl(claims.getClaimsMap());
		}
	}

	private class OpenIdResponseImpl implements OpenIdResponse {
		private String issuer;
		private String stateToken;
		private String code;

		public OpenIdResponseImpl(String issuer, String stateToken, String code) {
			this.issuer = issuer;
			this.stateToken = stateToken;
			this.code = code;
		}

		@Override
		public String getIssuer() {
			return issuer;
		}

		@Override
		public String getStateToken() {
			return stateToken;
		}

		@Override
		public String getCode() {
			return code;
		}

	}

	@Override
	public OpenIdResponse validateOpenIdResponse(OpenIdHeaders headers, URL url) throws OpenIdException {
		try {
			if (isDebug()) {
				LOGGER.info("validating openid response: " + url.toString());
			}
			Map<String, List<String>> parameters = StringUtils.splitQuery(url);
			List<String> stateTokens = parameters.get("state");
			if (stateTokens == null || stateTokens.isEmpty()) {
				throw new OpenIdException("missing state parameter");
			}
			String stateToken = stateTokens.get(0);
			JwtClaims claims = validateStateTokenInternal(stateToken);
			String issuerName = claims.getStringClaimValue(STATE_TOKEN_CLAIM_OPENID_ISSUER);
			if (issuerName == null) {
				throw new OpenIdException("state tokem missing issuer claim");
			}
			List<String> codes = parameters.get("code");
			if (codes == null || codes.isEmpty()) {
				List<String> errors = parameters.get("error");
				if (errors == null) {
					throw new OpenIdException("OpenId failed: missing code parameter");
				} else {
					List<String> errorDescriptions = parameters.get("error_description");
					if (errorDescriptions == null) {
						throw new OpenIdException(String.format("OpenId failed: %s", errors.get(0)));
					} else {
						throw new OpenIdException(
								String.format("OpenId failed: %s: %s", errors.get(0), errorDescriptions.get(0)));
					}
				}
			}

			String code = codes.get(0);

			return new OpenIdResponseImpl(issuerName, stateToken, code);

		} catch (MalformedClaimException exception) {
			throw new OpenIdException("Invalid state token", exception);
		} catch (UnsupportedEncodingException exception) {
			throw new OpenIdException("Could not parse url", exception);
		}
	}

	@Override
	public CompletableFutureBuilder<AuthClaims> requestId(String stateToken, String authenicationCode)
			throws OpenIdException {
		assertReady();
		if (stateToken == null || stateToken.isEmpty()) {
			throw new IllegalArgumentException("stateToken cannot be null or empty");
		}
		if (authenicationCode == null || authenicationCode.isEmpty()) {
			throw new IllegalArgumentException("authenicationCode cannot be null or empty");
		}
		JwtClaims claims = validateStateTokenInternal(stateToken);

		try {
			String issuerName = claims.getClaimValue(STATE_TOKEN_CLAIM_OPENID_ISSUER, String.class);
			String userState = claims.getClaimValue(STATE_TOKEN_CLAIM_OPENID_USER_STATE, String.class);

			IssuerDefinition issuer = getIssuer(issuerName);

			return new CompletableFutureBuilderImpl<AuthClaims>(this, issuer, OpenIdPropertyDef.Usage.TOKEN,
					(properties) -> {
						return getDiscovery(issuer).build().thenCompose((discovery) -> {
							try {
								Map<String, String> queryParameters = OpenIdProperty
										.propertyMapFor(OpenIdPropertyDef.RestPart.QUERY, properties);

								Map<String, String> bodyProperties = OpenIdProperty
										.propertyMapFor(OpenIdPropertyDef.RestPart.BODY, properties);
								bodyProperties.put("code", authenicationCode);

								URI tokenUri = new URIBuilder(issuer.getDiscovery().getTokenEndpoint())
										.setParameters(queryParameters).build();

								String contentType = "text/plain";
								String requestBody;
								if (issuer.getConfig().isTokenJsonRequestBodySupported()) {
									contentType = "application/json";
									requestBody = objectMapper.writerWithDefaultPrettyPrinter()
											.writeValueAsString(bodyProperties);
								} else {
									contentType = "application/x-www-form-urlencoded";
									requestBody = StringUtils.encodeAsWwwFormUrlEncoded(bodyProperties);
								}
								if (this.isDebug()) {
									LOGGER.info("retrieving token from issuer {} at {} with {}", issuer.getIssuerName(),
											tokenUri, requestBody);
								}

								HttpClient client = HttpClient.newHttpClient();
								HttpRequest request = HttpRequest.newBuilder().uri(tokenUri)
										.timeout(issuer.getTokenTimeout()).header("Content-Type", contentType)
										.header("charset", "utf-8").POST(BodyPublishers.ofString(requestBody)).build();

								return client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
										.thenApply((String jsonText) -> {
											try {
												if (isDebug()) {
													LOGGER.info("token request responded with: " + jsonText);
												}
												AuthClaims openidClaims = parseToken(issuer, jsonText,
														issuer.getClientId(), OpenIdTokenType.ID_TOKEN);
												if (openidClaims != null) {
													openidClaims.setClaim("issuer", issuerName);
													openidClaims.setClaim("state", userState);
												}
												return openidClaims;
											} catch (OpenIdException | JoseException | InvalidJwtException
													| IOException exception) {
												if (isDebug()) {
													LOGGER.info("token request failed: " + exception.getMessage()
															+ "; json response from issuer: " + jsonText);
												}
												throw new CompletionException(exception);
											}
										});
							} catch (URISyntaxException | IOException exception) {
								throw new CompletionException(exception);
							}
						});
					});
		} catch (MalformedClaimException exception) {
			throw new OpenIdException(exception);
		}
	}

	@Override
	public CompletableFutureBuilder<Boolean> revoke(String issuerName) throws OpenIdException {
		assertReady();
		IssuerDefinition issuer = getIssuer(issuerName);
		return new CompletableFutureBuilderImpl<Boolean>(this, issuer, OpenIdPropertyDef.Usage.REVOCATION,
				(properties) -> {
					return CompletableFuture.completedFuture(true);
				});
	}

	@Override
	public CompletableFutureBuilder<Boolean> endSession(String issuerName) throws OpenIdException {
		assertReady();
		IssuerDefinition issuer = getIssuer(issuerName);
		return new CompletableFutureBuilderImpl<Boolean>(this, issuer, OpenIdPropertyDef.Usage.END_SESSION,
				(properties) -> {
					return CompletableFuture.completedFuture(true);
				});
	}

	@Override
	public CompletableFutureBuilder<String> createBearerToken(int notBeforeInMinutes, int expiryInMinutes)
			throws OpenIdException {
		assertReady();
		return new CompletableFutureBuilderImpl<String>(this, (properties) -> {
			// Create the set of claims for the state token

			try {
				JwtClaims claims = new JwtClaims();
				claims.setIssuer(getDomain());
				claims.setAudience(getDomain());
				claims.setExpirationTimeMinutesInTheFuture(expiryInMinutes);
				claims.setGeneratedJwtId();
				claims.setIssuedAtToNow();
				claims.setNotBeforeMinutesInThePast(notBeforeInMinutes); // time before which the token is not yet valid
																			// (2 minutes ago)
				claims.setSubject(BEARER_TOKEN_SUBJECT);
				properties.forEach((property) -> {
					claims.setClaim(property.getName(), property.getValue());
				});

				JsonWebEncryption jwe = new JsonWebEncryption();

				jwe.setContentTypeHeaderValue("JWT");

				jwe.setPlaintext(claims.toJson());

				jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);

				jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);

				jwe.setKey(getBearerTokenKey().getKey());

				String stateToken = jwe.getCompactSerialization();

				return CompletableFuture.completedFuture(stateToken);
			} catch (JoseException exception) {
				throw new CompletionException(exception);
			}
		});
	}

	@Override
	public CompletableFuture<AuthClaims> validateBearerToken(String bearerToken) throws OpenIdException {
		assertReady();
		return CompletableFuture.supplyAsync(new Supplier<AuthClaims>() {
			@Override
			public AuthClaims get() {
				try {
					JsonWebEncryption jwe = new JsonWebEncryption();

					AlgorithmConstraints algConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST,
							KeyManagementAlgorithmIdentifiers.DIRECT);
					jwe.setAlgorithmConstraints(algConstraints);

					AlgorithmConstraints encConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST,
							ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
					jwe.setContentEncryptionAlgorithmConstraints(encConstraints);

					// Set the compact serialization on new Json Web Encryption object
					jwe.setCompactSerialization(bearerToken);

					jwe.setKey(getStateTokenKey().getKey());

					// Decrypt the JWE
					String stateJson = jwe.getPlaintextString();
					JwtClaims jwtClaims = JwtClaims.parse(stateJson);
					if (jwtClaims.getIssuer() == null || !jwtClaims.getIssuer().equals(getDomain())) {
						throw new CompletionException(new OpenIdException("Invalid bearer token: issuer not valid"));
					}
					boolean audienceFound = false;
					if (jwtClaims.hasAudience()) {
						for (String audience : jwtClaims.getAudience()) {
							if (audience.equals(getDomain())) {
								audienceFound = true;
								break;
							}
						}
					}
					if (!audienceFound) {
						throw new CompletionException(new OpenIdException("Invalid bearer token: audience not valid"));
					}
					if (!BEARER_TOKEN_SUBJECT.equals(jwtClaims.getSubject())) {
						throw new CompletionException(new OpenIdException("Invalid bearer token: subject not valid"));
					}
					return new AuthClaimsImpl(jwtClaims.getClaimsMap());

				} catch (JoseException | InvalidJwtException | MalformedClaimException exception) {
					throw new CompletionException(new OpenIdException("Invalid bearer token", exception));
				}
			}
		});
	}

	@Override
	public void close() {
		for (Map.Entry<String, IssuerDefinition> entry : this.issuers.entrySet()) {
			entry.getValue().close();
		}
		this.issuers.clear();
		this.state = ServiceState.CLOSED;
	}
}