package com.smallbankapp.banking.application.usecase.account.query;

import com.smallbankapp.banking.application.mediator.RequestHandler;
import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
import com.smallbankapp.banking.domain.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAccountHandler implements RequestHandler<GetAccountQuery, GetAccountResult> {

    private final AccountRepository accountRepository;

    @Override
    public GetAccountResult handle(GetAccountQuery query) {
        Account account = accountRepository.findById(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId().toString()));

        return new GetAccountResult(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber().getValue(),
                account.getAccountType().name(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency(),
                account.getStatus().name(),
                account.getCreatedAt()
        );
    }
}
