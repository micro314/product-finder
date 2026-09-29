package com.rosati.productfinder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest(properties = {
        "keycloak.issuer-uri=https://keycloak.example.com/realms/product-finder",
        "keycloak.jwk-set-uri=https://keycloak.example.com/realms/product-finder/protocol/openid-connect/certs"
})
class ProductFinderApplicationTests {

    private final ApplicationModules applicationModules = ApplicationModules.of(ProductFinderApplication.class);

    @Test
    void contextLoads() {
    }

    @Test
    void applicationModulesShouldBeValid() {
        applicationModules.verify();
    }

    @Test
    void writeDocumentation() {
        new Documenter(applicationModules).writeDocumentation();
    }
}
