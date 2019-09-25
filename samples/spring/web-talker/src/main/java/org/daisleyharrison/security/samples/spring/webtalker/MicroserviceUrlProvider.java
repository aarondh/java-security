package org.daisleyharrison.security.samples.spring.webtalker;

import java.util.Optional;

public interface MicroserviceUrlProvider {
    public Optional<String> lookupMicroservice(String microserviceName, boolean secure);
}