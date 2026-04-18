package com.smallbankapp.banking.application.usecase.transaction.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResult(
        UUID transactionId,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {}
