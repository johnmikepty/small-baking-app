package com.smallbankapp.banking.application.usecase.account.query;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
import com.smallbankapp.banking.domain.model.Account;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBalanceHandlerTest {

    @Mock AccountRepository accountRepository;
    @InjectMocks GetBalanceHandler handler;

    private Account mockAccount;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        mockAccount = new Account(
                accountId,
                UUID.randomUUID(),
                AccountNumber.of("1234567890"),
                AccountType.SAVINGS,
                Money.of(1250.50, "USD"),
                AccountStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void handle_existingAccount_returnsBalance() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        GetBalanceResult result = handler.handle(new GetBalanceQuery(accountId));

        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.accountNumber()).isEqualTo("1234567890");
        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.balance()).isPositive();
    }

    @Test
    void handle_nonExistingAccount_throwsAccountNotFoundException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetBalanceQuery(accountId)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
