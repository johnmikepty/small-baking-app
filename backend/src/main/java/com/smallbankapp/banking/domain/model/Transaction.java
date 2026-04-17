package com.smallbankapp.banking.domain.model;

import com.smallbankapp.banking.domain.valueobject.Money;
import com.smallbankapp.banking.domain.valueobject.TransactionStatus;
import com.smallbankapp.banking.domain.valueobject.TransactionType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a financial transaction.
 * Immutable after creation — transactions are never modified.
 */
public class Transaction {

    private final UUID id;
    private final UUID idempotencyKey;
    private final UUID sourceAccountId;
    private final UUID targetAccountId;
    private final TransactionType type;
    private final Money amount;
    private final String description;
    private final TransactionStatus status;
    private final Instant createdAt;

    public Transaction(UUID id, UUID idempotencyKey, UUID sourceAccountId, UUID targetAccountId,
                       TransactionType type, Money amount, String description,
                       TransactionStatus status, Instant createdAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Transaction createDeposit(UUID idempotencyKey, UUID targetAccountId,
                                            Money amount, String description) {
        return new Transaction(UUID.randomUUID(), idempotencyKey, null, targetAccountId,
                TransactionType.DEPOSIT, amount, description, TransactionStatus.COMPLETED, Instant.now());
    }

    public static Transaction createWithdrawal(UUID idempotencyKey, UUID sourceAccountId,
                                               Money amount, String description) {
        return new Transaction(UUID.randomUUID(), idempotencyKey, sourceAccountId, null,
                TransactionType.WITHDRAWAL, amount, description, TransactionStatus.COMPLETED, Instant.now());
    }

    public static Transaction createTransfer(UUID idempotencyKey, UUID sourceAccountId,
                                             UUID targetAccountId, Money amount, String description) {
        return new Transaction(UUID.randomUUID(), idempotencyKey, sourceAccountId, targetAccountId,
                TransactionType.TRANSFER, amount, description, TransactionStatus.COMPLETED, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public TransactionType getType() { return type; }
    public Money getAmount() { return amount; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
