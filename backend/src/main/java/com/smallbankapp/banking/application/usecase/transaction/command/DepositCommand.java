package com.smallbankapp.banking.application.usecase.transaction.command;

import com.smallbankapp.banking.application.mediator.Request;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositCommand(
        UUID idempotencyKey,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String description
) implements Request<TransactionResult> {}
