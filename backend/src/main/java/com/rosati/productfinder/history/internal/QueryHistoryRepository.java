package com.rosati.productfinder.history.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueryHistoryRepository extends JpaRepository<QueryHistoryEntry, Long> {
    List<QueryHistoryEntry> findByUserIdOrderBySubmittedAtDescIdDesc(String userId);

    Optional<QueryHistoryEntry> findByIdAndUserId(Long id, String userId);

    long deleteByUserId(String userId);
}
