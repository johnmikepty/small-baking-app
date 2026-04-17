package com.smallbankapp.banking.application.usecase.account.query;

import java.math.BigDecimal;
import java.util.UUID;

public record GetBalanceResult(
        UUID accountId,
        String accountNumber,
        BigDecimal balance,
        String currency
) {}
