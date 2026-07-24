package com.rosati.productfinder.history.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryHistoryRepository extends JpaRepository<QueryHistoryEntry, Long> {
    List<QueryHistoryEntry> findByUserIdOrderBySubmittedAtDescIdDesc(String userId);
}
