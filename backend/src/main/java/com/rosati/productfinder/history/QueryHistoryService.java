package com.rosati.productfinder.history;

import com.rosati.productfinder.history.internal.QueryHistoryEntry;
import com.rosati.productfinder.history.internal.QueryHistoryRepository;
import com.rosati.productfinder.product.ProductQuery;
import com.rosati.productfinder.product.ProductFilters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class QueryHistoryService {
    private final QueryHistoryRepository repository;

    public QueryHistoryService(QueryHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String userId, ProductQuery query) {
        String historyLabel = historyLabel(query);
        repository.deleteByUserIdAndQueryTextIgnoreCase(userId, historyLabel);
        repository.save(new QueryHistoryEntry(userId, historyLabel, query.limit(), Instant.now(), filtersJson(query.filters())));
    }

    private String filtersJson(ProductFilters filters) {
        if (filters.isEmpty()) return null;
        Map<String, Object> values = new java.util.LinkedHashMap<>();
        if (filters.name() != null && !filters.name().isBlank()) values.put("name", filters.name());
        values.put("source", filters.source()); values.put("manufacturer", filters.manufacturer());
        values.put("chipsetManufacturer", filters.chipsetManufacturer()); values.put("chipset", filters.chipset());
        values.put("memoryType", filters.memoryType()); values.put("minMemorySizeGb", filters.minMemorySizeGb());
        values.put("maxMemorySizeGb", filters.maxMemorySizeGb()); values.put("minBoostClockMhz", filters.minBoostClockMhz());
        values.put("maxBoostClockMhz", filters.maxBoostClockMhz()); values.put("minPrice", filters.minPrice());
        values.put("maxPrice", filters.maxPrice());
        return values.entrySet().stream().filter(entry -> entry.getValue() != null
                        && (!(entry.getValue() instanceof List<?> list) || !list.isEmpty()))
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(value(entry.getValue()), StandardCharsets.UTF_8))
                .collect(java.util.stream.Collectors.joining("&"));
    }

    private String historyLabel(ProductQuery query) {
        if (query.filters().isEmpty()) return query.text();
        var filters = query.filters();
        List<String> criteria = new ArrayList<>();
        if (filters.name() != null && !filters.name().isBlank()) criteria.add(filters.name());
        add(criteria, filters.source());
        add(criteria, filters.manufacturer());
        add(criteria, filters.chipsetManufacturer());
        add(criteria, filters.chipset());
        add(criteria, filters.memoryType());
        if (filters.minMemorySizeGb() != null || filters.maxMemorySizeGb() != null) {
            criteria.add(range(filters.minMemorySizeGb(), filters.maxMemorySizeGb(), "GB VRAM"));
        }
        if (filters.minBoostClockMhz() != null || filters.maxBoostClockMhz() != null) {
            criteria.add(range(filters.minBoostClockMhz(), filters.maxBoostClockMhz(), "MHz boost"));
        }
        if (filters.minPrice() != null || filters.maxPrice() != null) {
            criteria.add(range(filters.minPrice(), filters.maxPrice(), "price"));
        }
        if (!query.text().isBlank()) criteria.add(0, "Query: " + query.text());
        return String.join(" · ", criteria);
    }

    private String value(Object value) {
        return value instanceof List<?> list ? String.join(",", list.stream().map(String::valueOf).toList()) : String.valueOf(value);
    }

    private void add(List<String> criteria, List<String> values) {
        if (!values.isEmpty()) criteria.add(String.join(", ", values));
    }

    private String range(Object minimum, Object maximum, String unit) {
        if (minimum == null) return "up to " + maximum + " " + unit;
        if (maximum == null) return "from " + minimum + " " + unit;
        return minimum + "–" + maximum + " " + unit;
    }

    @Transactional(readOnly = true)
    public List<QueryHistoryItem> findForUser(String userId) {
        return repository.findByUserIdOrderBySubmittedAtDescIdDesc(userId).stream()
                .map(entry -> new QueryHistoryItem(entry.id(), entry.queryText(), entry.limit(), entry.submittedAt(),
                        originalQuery(entry), filters(entry)))
                .toList();
    }

    private String originalQuery(QueryHistoryEntry entry) {
        return filters(entry).isEmpty() ? entry.queryText() : extractQuery(entry.queryText());
    }

    private String extractQuery(String label) {
        return label.startsWith("Query: ") ? label.substring("Query: ".length()).split(" · ", 2)[0] : "";
    }

    private ProductFilters filters(QueryHistoryEntry entry) {
        if (entry.filtersJson() == null || entry.filtersJson().isBlank()) return ProductFilters.empty();
        Map<String, String> values = new HashMap<>();
        for (String pair : entry.filtersJson().split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) values.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
        }
        try {
            return new ProductFilters(string(values, "name"), list(values, "source"), list(values, "manufacturer"), list(values, "chipsetManufacturer"),
                    list(values, "chipset"), list(values, "memoryType"), integer(values, "minMemorySizeGb"),
                    integer(values, "maxMemorySizeGb"), integer(values, "minBoostClockMhz"), integer(values, "maxBoostClockMhz"),
                    decimal(values, "minPrice"), decimal(values, "maxPrice"));
        } catch (RuntimeException exception) {
            return ProductFilters.empty();
        }
    }

    private String string(Map<String, String> values, String key) { return values.get(key); }
    private Integer integer(Map<String, String> values, String key) { return values.get(key) == null ? null : Integer.valueOf(values.get(key)); }
    private java.math.BigDecimal decimal(Map<String, String> values, String key) { return values.get(key) == null ? null : new java.math.BigDecimal(values.get(key)); }
    private List<String> list(Map<String, String> values, String key) { return values.get(key) == null ? List.of() : List.of(values.get(key).split(",")); }

    @Transactional
    public void deleteForUser(String userId, Long entryId) {
        repository.findByIdAndUserId(entryId, userId).ifPresent(repository::delete);
    }

    @Transactional
    public void deleteAllForUser(String userId) {
        repository.deleteByUserId(userId);
    }

    public record QueryHistoryItem(Long id, String query, int limit, Instant submittedAt,
                                   String searchQuery, ProductFilters filters) {
    }
}
