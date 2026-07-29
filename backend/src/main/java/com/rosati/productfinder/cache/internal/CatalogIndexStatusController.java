package com.rosati.productfinder.cache.internal;

import com.rosati.productfinder.cache.CatalogCacheSearchService;
import com.rosati.productfinder.cache.CatalogIndexStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index")
class CatalogIndexStatusController {
    private final CatalogCacheSearchService cache;

    CatalogIndexStatusController(CatalogCacheSearchService cache) {
        this.cache = cache;
    }

    @GetMapping("/status")
    CatalogIndexStatus status() {
        return cache.status();
    }
}
