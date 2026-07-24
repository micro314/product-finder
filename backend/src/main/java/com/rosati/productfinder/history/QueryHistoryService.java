package com.rosati.productfinder.history;

import com.rosati.productfinder.history.internal.QueryHistoryEntry;
import com.rosati.productfinder.history.internal.QueryHistoryRepository;
import com.rosati.productfinder.product.ProductQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class QueryHistoryService {
    private final QueryHistoryRepository repository;

    public QueryHistoryService(QueryHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String userId, ProductQuery query) {
        repository.save(new QueryHistoryEntry(userId, query.text(), query.limit(), Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<QueryHistoryItem> findForUser(String userId) {
        return repository.findByUserIdOrderBySubmittedAtDescIdDesc(userId).stream()
                .map(entry -> new QueryHistoryItem(entry.queryText(), entry.limit(), entry.submittedAt()))
                .toList();
    }

    public record QueryHistoryItem(String query, int limit, Instant submittedAt) {
    }
}
