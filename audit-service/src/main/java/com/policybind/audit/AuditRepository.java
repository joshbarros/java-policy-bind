package com.policybind.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditEntry, String> {
    List<AuditEntry> findByQuoteIdOrderByRecordedAtAsc(String quoteId);
}
