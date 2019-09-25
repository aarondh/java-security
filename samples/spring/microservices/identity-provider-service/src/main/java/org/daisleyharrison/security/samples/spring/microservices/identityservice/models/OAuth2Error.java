package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OAuth2Error {
    /**
     * The request is missing a required parameter, includes an invalid parameter
     * value, includes a parameter more than once, or is otherwise malformed.
     */
    @JsonProperty("invalid_request")
    INVALID_REQUEST,

    /**
     * The client is not authorized to request an authorization code using this
     * method.
     */
    @JsonProperty("unauthorized_client")
    UNAUTHORIZED_CLIENT,

    /**
     * The resource owner or authorization server denied the request.
     */
    @JsonProperty("access_denied")
    ACCESS_DENIED,

    /**
     * The authorization server does not support obtaining a authorization code
     * using this method.
     */
    @JsonProperty("unsupported_response_type")
    UNSUPPORTED_RESPONSE_TYPE,

    /**
     * The requested scope is invalid, unknown, or malformed.
     */
    @JsonProperty("invalid_scope")
    INVALID_SCOPE,

    /**
     * The authorization server encountered an unexpected condition that prevented
     * it from fulfilling the request. (This error code is needed because a 500
     * Internal Server Error HTTP status code cannot be returned to the client via
     * an HTTP redirect.)
     */
    @JsonProperty("server_error")
    SERVER_ERROR,

    /**
     * The authorization server is currently unable to handle the request due to a
     * temporary overloading or maintenance of the server. (This error code is
     * needed because a 503 Service Unavailable HTTP status code cannot be returned
     * to the client via an HTTP redirect.)
     */
    @JsonProperty("temporarily_unavailable")
    TEMPORARILY_UNAVAILABLE

}