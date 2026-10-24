package com.policybind.quote;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<QuoteEntity, String> {
    Optional<QuoteEntity> findByRequestId(String requestId);
}
