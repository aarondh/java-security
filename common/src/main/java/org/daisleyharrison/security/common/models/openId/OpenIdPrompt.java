package org.daisleyharrison.security.common.models.openId;

public enum OpenIdPrompt {
    NONE("none"), CONSENT("consent"), SELECT_ACCOUNT("account");

    private String label;

    OpenIdPrompt(String label) {
        this.label = label;
    }

    public String toString() {
        return this.label;
    }
}