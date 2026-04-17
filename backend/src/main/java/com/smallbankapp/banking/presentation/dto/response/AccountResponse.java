package com.smallbankapp.banking.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        UUID userId,
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String currency,
        String status,
        Instant createdAt
) {}
