package com.smallbankapp.banking.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {}
