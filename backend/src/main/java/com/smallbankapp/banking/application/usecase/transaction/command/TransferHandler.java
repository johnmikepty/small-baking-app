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
public class TransferHandler implements RequestHandler<TransferCommand, TransactionResult> {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransactionResult handle(TransferCommand command) {
        return transactionRepository.findByIdempotencyKey(command.idempotencyKey())
                .map(this::toResult)
                .orElseGet(() -> processTransfer(command));
    }

    private TransactionResult processTransfer(TransferCommand command) {
        Account source = accountRepository.findById(command.sourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.sourceAccountId().toString()));

        Account target = accountRepository.findById(command.targetAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.targetAccountId().toString()));

        Money amount = Money.of(command.amount(), command.currency());
        // Domain rules: throws InsufficientFundsException if balance < amount
        source.withdraw(amount);
        target.deposit(amount);

        accountRepository.save(source);
        accountRepository.save(target);

        Transaction transaction = Transaction.createTransfer(
                command.idempotencyKey(),
                source.getId(),
                target.getId(),
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
