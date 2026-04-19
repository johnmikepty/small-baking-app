package com.smallbankapp.banking.application.usecase.transaction.query;

import com.smallbankapp.banking.application.mediator.Request;
import org.springframework.data.domain.Page;

import java.util.UUID;

public record GetTransactionHistoryQuery(
        UUID accountId,
        int page,
        int size
) implements Request<Page<TransactionHistoryResult>> {}
