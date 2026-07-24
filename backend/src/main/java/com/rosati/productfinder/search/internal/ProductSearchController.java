package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.product.ProductQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/products")
class ProductSearchController {
    private final ProductSearchService searchService;

    ProductSearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    SearchResponse search(
            @RequestParam("q") @NotBlank String query,
            @RequestParam(defaultValue = "20") @Min(1) @Max(ProductQuery.MAX_LIMIT) int limit
    ) {
        return searchService.search(new ProductQuery(query, limit));
    }
}
