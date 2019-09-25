package org.daisleyharrison.security.samples.spring.microservices.identityservice.beans;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthenticationClient;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.AuthenticationClientProvider;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.Scope;

import org.springframework.stereotype.Component;

@Component
public class TestAuthenticationClientProviderBean implements AuthenticationClientProvider {
    private static final Map<String, AuthenticationClient> s_clients = new HashMap<>();

    public TestAuthenticationClientProviderBean() {
        AuthenticationClient clientA = new AuthenticationClient();
        clientA.setClientId("test");
        clientA.setClientType(AuthenticationClient.ClientType.CONFIDENTIAL);
        clientA.setAllowsGetMethod(true);
        clientA.addAccessToken("pa55w0rd1", Duration.ofDays(365));
        clientA.getReturnUris().add("http://localhost/authReturn");
        clientA.getScopes().add(new Scope("profile", "The client profile"));

        s_clients.put(clientA.getClientId(), clientA);
        s_clients.put(clientA.get_id(), clientA);

        AuthenticationClient clientB = new AuthenticationClient();
        clientB.setClientId("testb");
        clientB.setClientType(AuthenticationClient.ClientType.PUBLIC);
        clientB.addAccessToken("pa55w0rd2", Duration.ofDays(365));
        clientB.getReturnUris().add("http://localhost/authReturn");
        clientB.getScopes().add(new Scope("profile", "The client profile"));
        clientB.getScopes().add(new Scope("platform/read", "The reading platform info"));
        clientB.getScopes().add(new Scope("weakness/read", "The reading platform info"));
        clientB.getScopes().add(new Scope("vunerability/read", "The reading platform info"));
        clientB.getScopes().add(new Scope("email", "The client email"));

        s_clients.put(clientB.getClientId(), clientB);
        s_clients.put(clientB.get_id(), clientB);
    }

    @Override
    public AuthenticationClient find(String clientId) {
        if (clientId == null) {
            return null;
        }
        clientId = clientId.toLowerCase();
        return s_clients.get(clientId);
    }

    @Override
    public AuthenticationClient authenticate(String clientId, String token) {
        if (clientId == null) {
            return null;
        }
        clientId = clientId.toLowerCase();
        AuthenticationClient client = s_clients.get(clientId);
        if(client != null){
            if(client.hasAccessToken(token)){
                return client;
            }
        }
        throw new SecurityException("Client not authenicated");
    }

}