package com.smallbankapp.banking.infrastructure.persistence.jpa.repository;

import com.smallbankapp.banking.infrastructure.persistence.jpa.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    Optional<TransactionJpaEntity> findByIdempotencyKey(UUID idempotencyKey);

    @Query("SELECT t FROM TransactionJpaEntity t WHERE " +
           "t.sourceAccountId = :accountId OR t.targetAccountId = :accountId " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionJpaEntity> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);
}
