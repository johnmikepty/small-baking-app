package com.smallbankapp.banking.infrastructure.persistence.jpa.repository;

import com.smallbankapp.banking.infrastructure.persistence.jpa.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);
    Optional<AccountJpaEntity> findByUserId(UUID userId);
    boolean existsByAccountNumber(String accountNumber);
}
