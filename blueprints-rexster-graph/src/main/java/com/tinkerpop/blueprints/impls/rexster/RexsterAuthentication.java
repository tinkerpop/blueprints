package com.tinkerpop.blueprints.impls.rexster;

import org.apache.commons.codec.binary.Base64;

public class RexsterAuthentication {

    private final String username;
    private final String password;

    public RexsterAuthentication(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public boolean isAuthenticationEnabled() {
        return this.username != null && this.password != null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthenticationHeaderValue() {
        return "Basic " + Base64.encodeBase64URLSafeString((username + ":" + password).getBytes());
    }
}
