package com.smallbankapp.banking.application.usecase.account.query;

import com.smallbankapp.banking.application.mediator.RequestHandler;
import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
import com.smallbankapp.banking.domain.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBalanceHandler implements RequestHandler<GetBalanceQuery, GetBalanceResult> {

    private final AccountRepository accountRepository;

    @Override
    public GetBalanceResult handle(GetBalanceQuery query) {
        Account account = accountRepository.findById(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId().toString()));

        return new GetBalanceResult(
                account.getId(),
                account.getAccountNumber().getValue(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency()
        );
    }
}
