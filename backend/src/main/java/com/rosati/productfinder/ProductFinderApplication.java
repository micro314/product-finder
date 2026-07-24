package com.rosati.productfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@Modulith(
        systemName = "ProductFinder"
)
public class ProductFinderApplication {

    static void main(String[] args) {
        SpringApplication.run(ProductFinderApplication.class, args);
    }
}
