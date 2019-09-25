package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import org.daisleyharrison.security.common.utilities.JwtClaimsImpl;

public class CodeClaims extends JwtClaimsImpl {
    public static class ReservedClaimNames {
        public static final String SCOPE = "sc";
        public static final String CLIENT_ID = "cid";
        public static final String REDIRECT_URI = "ru";
        public static final String STATE = "st";
        public static final String NONCE = "nc";
        public static final String CODE_CHALLENGE = "cc";
        public static final String CODE_CHALLENGE_METHOD = "ccm";
        public static final String CONFIRMATION = "cnf";
        public static final String PASSWORD = "pwd";
    }
    public CodeClaims() {

    }
}