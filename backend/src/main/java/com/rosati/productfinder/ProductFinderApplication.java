package com.rosati.productfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulith(
        systemName = "ProductFinder"
)
@EnableScheduling
public class ProductFinderApplication {

    static void main(String[] args) {
        SpringApplication.run(ProductFinderApplication.class, args);
    }
}
