package com.smallbankapp.banking.application.usecase.transaction.command;

import com.smallbankapp.banking.application.mediator.Request;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCommand(
        UUID idempotencyKey,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String currency,
        String description
) implements Request<TransactionResult> {}
