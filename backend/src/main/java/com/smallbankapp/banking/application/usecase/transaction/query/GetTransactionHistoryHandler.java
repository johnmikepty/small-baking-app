package com.smallbankapp.banking.application.usecase.transaction.query;

import com.smallbankapp.banking.application.mediator.RequestHandler;
import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.TransactionRepository;
import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
import com.smallbankapp.banking.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTransactionHistoryHandler
        implements RequestHandler<GetTransactionHistoryQuery, Page<TransactionHistoryResult>> {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public Page<TransactionHistoryResult> handle(GetTransactionHistoryQuery query) {
        // Verify account exists
        accountRepository.findById(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId().toString()));

        PageRequest pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return transactionRepository
                .findByAccountId(query.accountId(), pageable)
                .map(this::toResult);
    }

    private TransactionHistoryResult toResult(Transaction t) {
        return new TransactionHistoryResult(
                t.getId(),
                t.getType().name(),
                t.getAmount().getAmount(),
                t.getAmount().getCurrency(),
                t.getSourceAccountId(),
                t.getTargetAccountId(),
                t.getDescription(),
                t.getStatus().name(),
                t.getCreatedAt()
        );
    }
}
