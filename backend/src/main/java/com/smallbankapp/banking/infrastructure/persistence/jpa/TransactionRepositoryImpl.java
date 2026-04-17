package com.smallbankapp.banking.infrastructure.persistence.jpa;

import com.smallbankapp.banking.application.port.out.TransactionRepository;
import com.smallbankapp.banking.domain.model.Transaction;
import com.smallbankapp.banking.infrastructure.persistence.jpa.mapper.TransactionMapper;
import com.smallbankapp.banking.infrastructure.persistence.jpa.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(transaction)));
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(UUID idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public Page<Transaction> findByAccountId(UUID accountId, Pageable pageable) {
        return jpaRepository.findByAccountId(accountId, pageable).map(mapper::toDomain);
    }
}
