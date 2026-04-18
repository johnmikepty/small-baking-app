package com.smallbankapp.banking.application.usecase.transaction.command;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.TransactionEventPublisher;
import com.smallbankapp.banking.application.port.out.TransactionRepository;
import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
import com.smallbankapp.banking.domain.exception.InsufficientFundsException;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.model.Transaction;
import com.smallbankapp.banking.domain.valueobject.AccountNumber;
import com.smallbankapp.banking.domain.valueobject.AccountStatus;
import com.smallbankapp.banking.domain.valueobject.AccountType;
import com.smallbankapp.banking.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferHandlerTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock TransactionEventPublisher eventPublisher;
    @InjectMocks TransferHandler handler;

    private Account sourceAccount;
    private Account targetAccount;
    private TransferCommand command;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account(
                UUID.randomUUID(), UUID.randomUUID(),
                AccountNumber.of("1111111111"), AccountType.SAVINGS,
                Money.of(500.0, "USD"), AccountStatus.ACTIVE,
                Instant.now(), Instant.now()
        );
        targetAccount = new Account(
                UUID.randomUUID(), UUID.randomUUID(),
                AccountNumber.of("2222222222"), AccountType.SAVINGS,
                Money.of(100.0, "USD"), AccountStatus.ACTIVE,
                Instant.now(), Instant.now()
        );
        command = new TransferCommand(
                UUID.randomUUID(),
                sourceAccount.getId(), targetAccount.getId(),
                BigDecimal.valueOf(200.0), "USD", "Test transfer"
        );
    }

    @Test
    void handle_validTransfer_movesBalanceAndReturnsResult() {
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(command.sourceAccountId()))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(command.targetAccountId()))
                .thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResult result = handler.handle(command);

        assertThat(result.type()).isEqualTo("TRANSFER");
        assertThat(result.status()).isEqualTo("COMPLETED");
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(eventPublisher).publish(any(Transaction.class));
    }

    @Test
    void handle_insufficientFunds_throwsInsufficientFundsException() {
        TransferCommand bigTransfer = new TransferCommand(
                UUID.randomUUID(),
                sourceAccount.getId(), targetAccount.getId(),
                BigDecimal.valueOf(9999.0), "USD", "Too much"
        );
        when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(sourceAccount.getId()))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccount.getId()))
                .thenReturn(Optional.of(targetAccount));

        assertThatThrownBy(() -> handler.handle(bigTransfer))
                .isInstanceOf(InsufficientFundsException.class);

        verify(transactionRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void handle_targetAccountNotFound_throwsAccountNotFoundException() {
        when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(command.sourceAccountId()))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(command.targetAccountId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void handle_idempotentRequest_returnsExistingTransaction() {
        Transaction existing = Transaction.createTransfer(
                command.idempotencyKey(), sourceAccount.getId(),
                targetAccount.getId(), Money.of(200.0, "USD"), "Test transfer");
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey()))
                .thenReturn(Optional.of(existing));

        TransactionResult result = handler.handle(command);

        assertThat(result.type()).isEqualTo("TRANSFER");
        verify(accountRepository, never()).findById(any());
    }
}
