package org.daisleyharrison.security.common.models.openId;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenIdDiscovery {
    private String issuer;
    private String authorization_endpoint;
    private String token_endpoint;
    private String userinfo_endpoint;
    private String revocation_endpoint;
    private String end_session_endpoint;
    private String jwks_uri;
    private String[] response_types_supported;
    private String[] response_modes_supported;
    private String[] subject_types_supported;
    private String[] id_token_signing_alg_values_supported;
    private boolean http_logout_supported;
    private boolean frontchannel_logout_supported;
    private boolean frontchannel_logout_session_supported;
    private boolean request_uri_parameter_supported;
    private String[] scopes_supported;
    private String[] token_endpoint_auth_methods_supported;
    private String[] claims_supported;
    private String[] code_challenge_methods_supported;
    private String tenant_region_scope;
    private String cloud_instance_name;
    private String cloud_graph_host_name;
    private String msgraph_host;
    private String rbac_url;

    public OpenIdDiscovery() {

    }

    @JsonProperty("issuer")
    public String getIssuer() {
        return issuer;
    }

    @JsonProperty("issuer")
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @JsonProperty("authorization_endpoint")
    public String getAuthorizationEndpoint() {
        return authorization_endpoint;
    }

    @JsonProperty("authorization_endpoint")
    public void setAuthorizationEndpoint(String authorization_endpoint) {
        this.authorization_endpoint = authorization_endpoint;
    }

    @JsonProperty("token_endpoint")
    public String getTokenEndpoint() {
        return token_endpoint;
    }

    @JsonProperty("token_endpoint")
    public void setTokenEndpoint(String token_endpoint) {
        this.token_endpoint = token_endpoint;
    }

    @JsonProperty("userinfo_endpoint")
    public String getUserinfoEndpoint() {
        return userinfo_endpoint;
    }

    @JsonProperty("userinfo_endpoint")
    public void setUserinfoEndpoint(String userinfo_endpoint) {
        this.userinfo_endpoint = userinfo_endpoint;
    }

    @JsonProperty("revocation_endpoint")
    public String getRevocationEndpoint() {
        return revocation_endpoint;
    }

    @JsonProperty("revocation_endpoint")
    public void setRevocationEndpoint(String revocation_endpoint) {
        this.revocation_endpoint = revocation_endpoint;
    }

    @JsonProperty("end_session_endpoint")
    public String getEndSessionEndpoint() {
        return end_session_endpoint;
    }

    @JsonProperty("end_session_endpoint")
    public void setEndSessionEndpoint(String end_session_endpoint) {
        this.end_session_endpoint = end_session_endpoint;
    }

    @JsonProperty("jwks_uri")
    public String getJwksUri() {
        return jwks_uri;
    }

    @JsonProperty("jwks_uri")
    public void setJwksUri(String jwks_uri) {
        this.jwks_uri = jwks_uri;
    }

    @JsonProperty("response_types_supported")
    public String[] getResponseTypesSupported() {
        return response_types_supported;
    }

    @JsonProperty("response_types_supported")
    public void setResponseTypesSupported(String[] response_types_supported) {
        this.response_types_supported = response_types_supported;
    }

    @JsonProperty("response_modes_supported")
    public String[] getResponseModesSupported() {
        return response_modes_supported;
    }

    @JsonProperty("response_modes_supported")
    public void setResponseModesSupported(String[] response_modes_supported) {
        this.response_modes_supported = response_modes_supported;
    }

    @JsonProperty("subject_types_supported")
    public String[] getSubjectTypesSupported() {
        return subject_types_supported;
    }

    @JsonProperty("subject_types_supported")
    public void setSubjectTypesSupported(String[] subject_types_supported) {
        this.subject_types_supported = subject_types_supported;
    }

    @JsonProperty("id_token_signing_alg_values_supported")
    public String[] getIdTokenSigningAlgValuesSupported() {
        return id_token_signing_alg_values_supported;
    }

    @JsonProperty("id_token_signing_alg_values_supported")
    public void setIdTokenSigningAlgValuesSupported(String[] id_token_signing_alg_values_supported) {
        this.id_token_signing_alg_values_supported = id_token_signing_alg_values_supported;
    }

    @JsonProperty("scopes_supported")
    public String[] getScopesSupported() {
        return scopes_supported;
    }

    @JsonProperty("scopes_supported")
    public void setScopesSupported(String[] scopes_supported) {
        this.scopes_supported = scopes_supported;
    }

    @JsonProperty("token_endpoint_auth_methods_supported")
    public String[] getTokenEndpointAuthMethodsSupported() {
        return token_endpoint_auth_methods_supported;
    }

    @JsonProperty("token_endpoint_auth_methods_supported")
    public void setTokenEndpointAuthMethodsSupported(String[] token_endpoint_auth_methods_supported) {
        this.token_endpoint_auth_methods_supported = token_endpoint_auth_methods_supported;
    }

    @JsonProperty("claims_supported")
    public String[] getClaimsSupported() {
        return claims_supported;
    }

    @JsonProperty("claims_supported")
    public void setClaimsSupported(String[] claims_supported) {
        this.claims_supported = claims_supported;
    }

    @JsonProperty("code_challenge_methods_supported")
    public String[] getCodeChallengeMethodsSupported() {
        return code_challenge_methods_supported;
    }

    @JsonProperty("code_challenge_methods_supported")
    public void setCodeChallengeMethodsSupported(String[] code_challenge_methods_supported) {
        this.code_challenge_methods_supported = code_challenge_methods_supported;
    }

    @JsonProperty("http_logout_supported")
    public boolean isHttpLogoutSupported() {
        return http_logout_supported;
    }

    @JsonProperty("http_logout_supported")
    public void setHttpLogoutSupported(boolean http_logout_supported) {
        this.http_logout_supported = http_logout_supported;
    }

    @JsonProperty("frontchannel_logout_supported")
    public boolean isFrontChannelLogoutSupported() {
        return frontchannel_logout_supported;
    }

    @JsonProperty("frontchannel_logout_supported")
    public void setFrontChannelLogoutSupported(boolean frontchannel_logout_supported) {
        this.frontchannel_logout_supported = frontchannel_logout_supported;
    }

    @JsonProperty("frontchannel_logout_session_supported")
    public boolean isFrontChannelLogoutSessionSupported() {
        return frontchannel_logout_session_supported;
    }

    @JsonProperty("frontchannel_logout_session_supported")
    public void setFrontChannelLogoutSessionSupported(boolean frontchannel_logout_session_supported) {
        this.frontchannel_logout_session_supported = frontchannel_logout_session_supported;
    }

    @JsonProperty("request_uri_parameter_supported")
    public boolean getRequestUriParameterSupported() {
        return request_uri_parameter_supported;
    }

    @JsonProperty("request_uri_parameter_supported")
    public void setRequestUriParameterSupported(boolean request_uri_parameter_supported) {
        this.request_uri_parameter_supported = request_uri_parameter_supported;
    }

    @JsonProperty("tenant_region_scope")
    public String getTenantRegionScope() {
        return tenant_region_scope;
    }

    @JsonProperty("tenant_region_scope")
    public void setTenantRegionScope(String tenant_region_scope) {
        this.tenant_region_scope = tenant_region_scope;
    }

    @JsonProperty("cloud_instance_name")
    public String getCloudInstanceName() {
        return cloud_instance_name;
    }

    @JsonProperty("cloud_instance_name")
    public void setCloudInstanceName(String cloud_instance_name) {
        this.cloud_instance_name = cloud_instance_name;
    }

    @JsonProperty("cloud_graph_host_name")
    public String getCloudGraphHhostName() {
        return cloud_graph_host_name;
    }

    @JsonProperty("cloud_graph_host_name")
    public void setCloudGraphHhostName(String cloud_graph_host_name) {
        this.cloud_graph_host_name = cloud_graph_host_name;
    }

    @JsonProperty("msgraph_host")
    public String getMsgraphHost() {
        return msgraph_host;
    }

    @JsonProperty("msgraph_host")
    public void setMsgraphHost(String msgraph_host) {
        this.msgraph_host = msgraph_host;
    }

    @JsonProperty("rbac_url")
    public String getRbacUrl() {
        return rbac_url;
    }

    @JsonProperty("rbac_url")
    public void setRbacUrl(String rbac_url) {
        this.rbac_url = rbac_url;
    }
}