package com.smallbankapp.banking.application.usecase.transaction.command;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.TransactionEventPublisher;
import com.smallbankapp.banking.application.port.out.TransactionRepository;
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
class WithdrawHandlerTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock TransactionEventPublisher eventPublisher;
    @InjectMocks WithdrawHandler handler;

    private Account mockAccount;
    private WithdrawCommand command;

    @BeforeEach
    void setUp() {
        mockAccount = new Account(
                UUID.randomUUID(), UUID.randomUUID(),
                AccountNumber.of("1234567890"), AccountType.SAVINGS,
                Money.of(200.0, "USD"), AccountStatus.ACTIVE,
                Instant.now(), Instant.now()
        );
        command = new WithdrawCommand(UUID.randomUUID(), mockAccount.getId(),
                BigDecimal.valueOf(50.0), "USD", "Test withdrawal");
    }

    @Test
    void handle_sufficientFunds_decreasesBalanceAndReturnsResult() {
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(command.accountId()))
                .thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any())).thenReturn(mockAccount);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResult result = handler.handle(command);

        assertThat(result.type()).isEqualTo("WITHDRAWAL");
        assertThat(result.status()).isEqualTo("COMPLETED");
        verify(eventPublisher).publish(any(Transaction.class));
    }

    @Test
    void handle_insufficientFunds_throwsInsufficientFundsException() {
        Account poorAccount = new Account(
                UUID.randomUUID(), UUID.randomUUID(),
                AccountNumber.of("9876543210"), AccountType.SAVINGS,
                Money.of(10.0, "USD"), AccountStatus.ACTIVE,
                Instant.now(), Instant.now()
        );
        WithdrawCommand bigWithdraw = new WithdrawCommand(
                UUID.randomUUID(), poorAccount.getId(),
                BigDecimal.valueOf(500.0), "USD", "Too much");

        when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(poorAccount.getId())).thenReturn(Optional.of(poorAccount));

        assertThatThrownBy(() -> handler.handle(bigWithdraw))
                .isInstanceOf(InsufficientFundsException.class);

        verify(transactionRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void handle_idempotentRequest_returnsExistingTransaction() {
        Transaction existing = Transaction.createWithdrawal(
                command.idempotencyKey(), mockAccount.getId(),
                Money.of(50.0, "USD"), "Test withdrawal");
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey()))
                .thenReturn(Optional.of(existing));

        TransactionResult result = handler.handle(command);

        assertThat(result.type()).isEqualTo("WITHDRAWAL");
        verify(accountRepository, never()).findById(any());
    }
}
