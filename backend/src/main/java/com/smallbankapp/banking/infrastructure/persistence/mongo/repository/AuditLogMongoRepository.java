package com.smallbankapp.banking.infrastructure.persistence.mongo.repository;

import com.smallbankapp.banking.infrastructure.persistence.mongo.document.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {
    Optional<AuditLogDocument> findByTransactionId(UUID transactionId);
    boolean existsByTransactionId(UUID transactionId);
}
