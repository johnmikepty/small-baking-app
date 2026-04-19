package com.smallbankapp.banking.application.usecase.transaction.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryResult(
        UUID transactionId,
        String type,
        BigDecimal amount,
        String currency,
        UUID sourceAccountId,
        UUID targetAccountId,
        String description,
        String status,
        Instant createdAt
) {}
