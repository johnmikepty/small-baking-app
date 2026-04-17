package com.smallbankapp.banking.application.port.out;

import com.smallbankapp.banking.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findByIdempotencyKey(UUID idempotencyKey);
    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);
}
