package com.smallbankapp.banking.application.usecase.transaction.command;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.TransactionEventPublisher;
import com.smallbankapp.banking.application.port.out.TransactionRepository;
import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
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
class DepositHandlerTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock TransactionEventPublisher eventPublisher;
    @InjectMocks DepositHandler handler;

    private Account mockAccount;
    private DepositCommand command;

    @BeforeEach
    void setUp() {
        mockAccount = new Account(
                UUID.randomUUID(), UUID.randomUUID(),
                AccountNumber.of("1234567890"), AccountType.SAVINGS,
                Money.of(100.0, "USD"), AccountStatus.ACTIVE,
                Instant.now(), Instant.now()
        );
        command = new DepositCommand(UUID.randomUUID(), mockAccount.getId(),
                BigDecimal.valueOf(50.0), "USD", "Test deposit");
    }

    @Test
    void handle_validDeposit_increasesBalanceAndReturnsResult() {
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(command.accountId()))
                .thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any())).thenReturn(mockAccount);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResult result = handler.handle(command);

        assertThat(result.type()).isEqualTo("DEPOSIT");
        assertThat(result.status()).isEqualTo("COMPLETED");
        verify(eventPublisher).publish(any(Transaction.class));
    }

    @Test
    void handle_idempotentRequest_returnsExistingTransaction() {
        Transaction existing = Transaction.createDeposit(
                command.idempotencyKey(), mockAccount.getId(),
                Money.of(50.0, "USD"), "Test deposit");
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey()))
                .thenReturn(Optional.of(existing));

        TransactionResult result = handler.handle(command);

        assertThat(result.type()).isEqualTo("DEPOSIT");
        verify(accountRepository, never()).findById(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void handle_accountNotFound_throwsAccountNotFoundException() {
        when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(command.accountId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
