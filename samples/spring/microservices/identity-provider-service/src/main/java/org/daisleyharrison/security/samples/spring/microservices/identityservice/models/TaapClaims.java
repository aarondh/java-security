package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import org.daisleyharrison.security.common.utilities.JwtClaimsImpl;

public class TaapClaims extends JwtClaimsImpl {
    public static class ReservedClaimNames {
        public static final String CONFIRMATION = "cnf";
    }
    public TaapClaims() {

    }
}