package com.rosati.productfinder.identity.internal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {
        "keycloak.issuer-uri=https://keycloak.example.com/realms/product-finder",
        "keycloak.jwk-set-uri=https://keycloak.example.com/realms/product-finder/protocol/openid-connect/certs",
        "keycloak.client-id=product-finder-web",
        "keycloak.redirect-uri=https://app.example.com/auth/callback"
})
class SecurityConfigurationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void rejectsAnonymousRequestsToAllApplicationEndpoints() throws Exception {
        mvc.perform(get("/api/products/search").param("q", "rtx 5070"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permitsAuthenticatedRequests() throws Exception {
        mvc.perform(get("/api/auth/me").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void permitsTheKeycloakLoginRedirect() throws Exception {
        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isFound());
    }
}
