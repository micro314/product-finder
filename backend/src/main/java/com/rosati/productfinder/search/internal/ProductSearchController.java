package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.history.QueryHistoryService;
import com.rosati.productfinder.product.ProductQuery;
import com.rosati.productfinder.product.ProductFilters;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Validated
@RestController
@RequestMapping("/api/products")
class ProductSearchController {
    private final ProductSearchService searchService;
    private final QueryHistoryService queryHistory;

    ProductSearchController(ProductSearchService searchService, QueryHistoryService queryHistory) {
        this.searchService = searchService;
        this.queryHistory = queryHistory;
    }

    @GetMapping("/search")
    SearchResponse search(
            @RequestParam("q") @NotBlank String query,
            @RequestParam(defaultValue = "20") @Min(1) @Max(ProductQuery.MAX_LIMIT) int limit,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String chipsetManufacturer,
            @RequestParam(required = false) String chipset,
            @RequestParam(required = false) String memoryType,
            @RequestParam(required = false) Integer minMemorySizeGb,
            @RequestParam(required = false) Integer maxMemorySizeGb,
            @RequestParam(required = false) Integer minBoostClockMhz,
            @RequestParam(required = false) Integer maxBoostClockMhz,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            Principal principal
    ) {
        ProductFilters filters = new ProductFilters(source, manufacturer, chipsetManufacturer,
                chipset, memoryType, minMemorySizeGb, maxMemorySizeGb,
                minBoostClockMhz, maxBoostClockMhz, minPrice, maxPrice);
        ProductQuery productQuery = new ProductQuery(query, limit, filters);
        if (principal instanceof JwtAuthenticationToken authentication) {
            queryHistory.record(authentication.getToken().getSubject(), productQuery);
        }
        return searchService.search(productQuery);
    }
}
