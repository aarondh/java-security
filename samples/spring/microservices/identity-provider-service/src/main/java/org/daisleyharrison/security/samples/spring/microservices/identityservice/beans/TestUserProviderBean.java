package org.daisleyharrison.security.samples.spring.microservices.identityservice.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.OpenIdClaims;
import org.daisleyharrison.security.common.utilities.OpenIdClaimsImpl;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthenticationClient;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthenticationClientProvider;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.Scope;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.UserProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUserProviderBean implements UserProvider {
    private static final ObjectMapper _objectMapper = new ObjectMapper();
    @Autowired
    AuthenticationClientProvider clientProvider;

    @SuppressWarnings("unchecked")
    private static OpenIdClaims toClaims(String subject, List<String> scopes, Object claimsSource) {
        OpenIdClaims claims = new OpenIdClaimsImpl();
        try {
            claims.setSubject(subject);
            JsonNode node = _objectMapper.valueToTree(claimsSource);
            Iterator<Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> field = fields.next();
                String name = field.getKey();
                JsonNode valueNode = field.getValue();
                Object value;
                if (valueNode.isArray()) {
                    List<String> values = new ArrayList<>();
                    Iterator<JsonNode> items = valueNode.iterator();

                    while (items.hasNext()) {
                        JsonNode item = items.next();
                        values.add(item.asText());
                    }
                    value = values;
                } else {
                    value = valueNode.asText();
                }
                claims.setClaim(name, value);
            }
            claims.unsetClaim(OpenIdClaims.ReservedClaims.SCOPE);
            claims.addScope(scopes);
        } catch (MalformedAuthClaimException ex) {

        }

        return claims;
    }

    @Override
    public OpenIdClaims find(String userId) {
        AuthenticationClient user = clientProvider.find(userId);
        return toClaims(userId, user.getScopes().stream().map(Scope::getName).collect(Collectors.toList()), user);
    }

    @Override
    public OpenIdClaims authenticate(String userId, String token) throws SecurityException {
        AuthenticationClient user = clientProvider.find(userId);
        if (user != null && user.hasAccessToken(token)) {
            return toClaims(userId, user.getScopes().stream().map(Scope::getName).collect(Collectors.toList()), user);
        }
        throw new SecurityException("User anthenication falied");
    }

}