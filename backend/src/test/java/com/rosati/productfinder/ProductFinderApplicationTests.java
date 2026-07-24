package com.rosati.productfinder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class ProductFinderApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void applicationModulesShouldBeValid() {
        ApplicationModules.of(ProductFinderApplication.class).verify();
    }
}
