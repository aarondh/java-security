package org.daisleyharrison.security.common.models.openId;

/**
 * Token types
 */
public enum OpenIdTokenType {
	ACCESS_TOKEN("access_token"), ID_TOKEN("id_token");

	private String token;

	OpenIdTokenType(String token) {
		this.token = token;
	}

	public String getTokenType() {
		return token;
	}
}
