package com.rosati.productfinder.identity.internal;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("keycloak")
record KeycloakProperties(URI issuerUri, URI jwkSetUri, String clientId, URI redirectUri) {

    boolean hasIssuer() {
        return issuerUri != null && !issuerUri.toString().isBlank();
    }

    boolean hasLoginConfiguration() {
        return hasIssuer() && clientId != null && !clientId.isBlank()
                && redirectUri != null && !redirectUri.toString().isBlank();
    }

    boolean hasJwkSetUri() {
        return jwkSetUri != null && !jwkSetUri.toString().isBlank();
    }
}
