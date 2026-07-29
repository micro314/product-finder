package com.rosati.productfinder.cache.internal;

import com.rosati.productfinder.cache.CatalogCacheSearchService;
import com.rosati.productfinder.cache.CatalogFilterOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index")
class CatalogFilterOptionsController {
    private final CatalogCacheSearchService cache;

    CatalogFilterOptionsController(CatalogCacheSearchService cache) {
        this.cache = cache;
    }

    @GetMapping("/filter-options")
    CatalogFilterOptions options() {
        return cache.filterOptions();
    }
}
