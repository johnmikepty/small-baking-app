package com.smallbankapp.banking.infrastructure.persistence.jpa.mapper;

import com.smallbankapp.banking.domain.model.Transaction;
import com.smallbankapp.banking.domain.valueobject.Money;
import com.smallbankapp.banking.domain.valueobject.TransactionStatus;
import com.smallbankapp.banking.domain.valueobject.TransactionType;
import com.smallbankapp.banking.infrastructure.persistence.jpa.entity.TransactionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getSourceAccountId(),
                entity.getTargetAccountId(),
                TransactionType.valueOf(entity.getTransactionType()),
                Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getDescription(),
                TransactionStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }

    public TransactionJpaEntity toEntity(Transaction domain) {
        return TransactionJpaEntity.builder()
                .id(domain.getId())
                .idempotencyKey(domain.getIdempotencyKey())
                .sourceAccountId(domain.getSourceAccountId())
                .targetAccountId(domain.getTargetAccountId())
                .transactionType(domain.getType().name())
                .amount(domain.getAmount().getAmount())
                .currency(domain.getAmount().getCurrency())
                .description(domain.getDescription())
                .status(domain.getStatus().name())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
