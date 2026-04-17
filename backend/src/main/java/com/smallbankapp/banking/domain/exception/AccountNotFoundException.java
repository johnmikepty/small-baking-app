package com.smallbankapp.banking.domain.exception;

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId);
    }
}
