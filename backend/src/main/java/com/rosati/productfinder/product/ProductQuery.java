package com.rosati.productfinder.product;

public record ProductQuery(String text, int limit, ProductFilters filters) {
    public static final int MAX_LIMIT = 100;

    public ProductQuery {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        text = text.trim();
        filters = filters == null ? ProductFilters.empty() : filters;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
    }

    public ProductQuery(String text, int limit) {
        this(text, limit, ProductFilters.empty());
    }
}
