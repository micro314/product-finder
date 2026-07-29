package com.rosati.productfinder.product;

public record ProductQuery(String text, int limit, ProductFilters filters) {
    public static final int MAX_LIMIT = 10_000;
    private static final int LEGACY_MAX_LIMIT = 100;

    public ProductQuery {
        filters = filters == null ? ProductFilters.empty() : filters;
        if ((text == null || text.isBlank()) && filters.isEmpty()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        text = text == null ? "" : text.trim();
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
    }

    public ProductQuery(String text, int limit) {
        this(text, validateLegacyLimit(limit), ProductFilters.empty());
    }

    private static int validateLegacyLimit(int limit) {
        if (limit < 1 || limit > LEGACY_MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + LEGACY_MAX_LIMIT);
        }
        return limit;
    }
}
