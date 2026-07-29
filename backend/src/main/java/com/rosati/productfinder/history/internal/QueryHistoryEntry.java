package com.rosati.productfinder.history.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "query_history",
        indexes = @Index(name = "query_history_user_submitted_idx", columnList = "user_id, submitted_at")
)
public class QueryHistoryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "query_text", nullable = false, length = 500)
    private String queryText;

    @Column(name = "requested_limit", nullable = false)
    private int requestedLimit;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    protected QueryHistoryEntry() {
    }

    public QueryHistoryEntry(String userId, String queryText, int requestedLimit, Instant submittedAt) {
        this.userId = userId;
        this.queryText = queryText;
        this.requestedLimit = requestedLimit;
        this.submittedAt = submittedAt;
    }

    public String queryText() {
        return queryText;
    }

    public Long id() {
        return id;
    }

    public int limit() {
        return requestedLimit;
    }

    public Instant submittedAt() {
        return submittedAt;
    }
}
