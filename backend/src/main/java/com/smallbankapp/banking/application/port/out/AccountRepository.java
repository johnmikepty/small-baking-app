package com.smallbankapp.banking.application.port.out;

import com.smallbankapp.banking.domain.model.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUserId(UUID userId);
    boolean existsByAccountNumber(String accountNumber);
}
