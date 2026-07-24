package com.rosati.productfinder.identity.internal;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
class AuthenticationController {
    private final KeycloakProperties keycloak;

    AuthenticationController(KeycloakProperties keycloak) {
        this.keycloak = keycloak;
    }

    @GetMapping("/login")
    ResponseEntity<Void> login() {
        return redirectTo("auth");
    }

    @GetMapping("/register")
    ResponseEntity<Void> register() {
        return redirectTo("registrations");
    }

    @GetMapping("/me")
    AuthenticatedUser currentUser(@AuthenticationPrincipal Jwt jwt) {
        return new AuthenticatedUser(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"));
    }

    private ResponseEntity<Void> redirectTo(String endpoint) {
        if (!keycloak.hasLoginConfiguration()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Keycloak is not configured. Set KEYCLOAK_ISSUER_URI, KEYCLOAK_CLIENT_ID, and KEYCLOAK_REDIRECT_URI.");
        }

        URI location = UriComponentsBuilder.fromUri(keycloak.issuerUri())
                .pathSegment("protocol", "openid-connect", endpoint)
                .queryParam("client_id", keycloak.clientId())
                .queryParam("redirect_uri", keycloak.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .build()
                .encode()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location.toString()).build();
    }

    record AuthenticatedUser(String subject, String username, String email) {
    }
}
