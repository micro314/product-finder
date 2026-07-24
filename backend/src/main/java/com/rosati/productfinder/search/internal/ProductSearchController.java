package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.history.QueryHistoryService;
import com.rosati.productfinder.product.ProductQuery;
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
            Principal principal
    ) {
        ProductQuery productQuery = new ProductQuery(query, limit);
        if (principal instanceof JwtAuthenticationToken authentication) {
            queryHistory.record(authentication.getToken().getSubject(), productQuery);
        }
        return searchService.search(productQuery);
    }
}
