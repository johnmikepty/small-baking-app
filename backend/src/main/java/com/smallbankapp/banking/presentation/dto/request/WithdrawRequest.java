package com.smallbankapp.banking.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawRequest(
        @NotNull UUID idempotencyKey,
        @NotNull UUID accountId,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount,
        @NotBlank String currency,
        String description
) {}
