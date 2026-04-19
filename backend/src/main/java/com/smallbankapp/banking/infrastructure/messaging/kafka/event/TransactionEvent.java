package com.smallbankapp.banking.infrastructure.messaging.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event payload for every completed transaction.
 * Published to topic: banking.transactions
 * Dead-letter topic:  banking.transactions.DLT
 */
public record TransactionEvent(
        UUID transactionId,
        UUID sourceAccountId,
        UUID targetAccountId,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        Instant timestamp
) {
    /** Convenience factory from infrastructure-layer data. */
    public static TransactionEvent of(
            UUID transactionId,
            UUID sourceAccountId,
            UUID targetAccountId,
            String type,
            BigDecimal amount,
            String currency,
            String status,
            Instant timestamp) {
        return new TransactionEvent(
                transactionId, sourceAccountId, targetAccountId,
                type, amount, currency, status, timestamp);
    }
}
