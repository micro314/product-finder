package com.rosati.productfinder.identity.internal;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest {

    @Test
    void redirectsLoginToKeycloakAuthorizationEndpoint() throws Exception {
        MockMvc mvc = controllerWithKeycloak();

        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location",
                        "https://keycloak.example.com/realms/product-finder/protocol/openid-connect/auth"
                                + "?client_id=product-finder-web&redirect_uri=https://app.example.com/auth/callback"
                                + "&response_type=code&scope=openid%20profile%20email"));
    }

    @Test
    void redirectsRegistrationToKeycloakRegistrationEndpoint() throws Exception {
        MockMvc mvc = controllerWithKeycloak();

        mvc.perform(get("/api/auth/register"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location",
                        "https://keycloak.example.com/realms/product-finder/protocol/openid-connect/registrations"
                                + "?client_id=product-finder-web&redirect_uri=https://app.example.com/auth/callback"
                                + "&response_type=code&scope=openid%20profile%20email"));
    }

    @Test
    void reportsMissingKeycloakConfiguration() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new AuthenticationController(new KeycloakProperties(null, null, null, null))).build();

        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isServiceUnavailable());
    }

    private MockMvc controllerWithKeycloak() {
        return MockMvcBuilders.standaloneSetup(new AuthenticationController(new KeycloakProperties(
                URI.create("https://keycloak.example.com/realms/product-finder"),
                null,
                "product-finder-web",
                URI.create("https://app.example.com/auth/callback")))).build();
    }
}
