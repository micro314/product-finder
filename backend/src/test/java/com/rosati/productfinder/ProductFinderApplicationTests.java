package com.rosati.productfinder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest(properties = {
        "keycloak.issuer-uri=https://keycloak.example.com/realms/product-finder",
        "keycloak.jwk-set-uri=https://keycloak.example.com/realms/product-finder/protocol/openid-connect/certs"
})
class ProductFinderApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void applicationModulesShouldBeValid() {
        ApplicationModules.of(ProductFinderApplication.class).verify();
    }
}
