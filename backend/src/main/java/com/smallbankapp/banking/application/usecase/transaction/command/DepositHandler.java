package com.smallbankapp.banking.application.usecase.transaction.command;

import com.smallbankapp.banking.application.mediator.RequestHandler;
import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.TransactionEventPublisher;
import com.smallbankapp.banking.application.port.out.TransactionRepository;
import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.model.Transaction;
import com.smallbankapp.banking.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DepositHandler implements RequestHandler<DepositCommand, TransactionResult> {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransactionResult handle(DepositCommand command) {
        // Idempotency check
        return transactionRepository.findByIdempotencyKey(command.idempotencyKey())
                .map(this::toResult)
                .orElseGet(() -> processDeposit(command));
    }

    private TransactionResult processDeposit(DepositCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId().toString()));

        Money amount = Money.of(command.amount(), command.currency());
        account.deposit(amount);
        accountRepository.save(account);

        Transaction transaction = Transaction.createDeposit(
                command.idempotencyKey(),
                account.getId(),
                amount,
                command.description()
        );
        transaction = transactionRepository.save(transaction);
        eventPublisher.publish(transaction);

        return toResult(transaction);
    }

    private TransactionResult toResult(Transaction t) {
        return new TransactionResult(
                t.getId(),
                t.getType().name(),
                t.getAmount().getAmount(),
                t.getAmount().getCurrency(),
                t.getStatus().name(),
                t.getCreatedAt()
        );
    }
}
