package com.smallbankapp.banking.infrastructure.persistence.mongo.repository;

import com.smallbankapp.banking.infrastructure.persistence.mongo.document.TransactionHistoryDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface TransactionHistoryMongoRepository extends MongoRepository<TransactionHistoryDocument, String> {
    Page<TransactionHistoryDocument> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}
