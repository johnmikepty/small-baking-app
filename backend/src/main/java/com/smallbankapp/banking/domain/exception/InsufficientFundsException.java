package com.smallbankapp.banking.domain.exception;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(String accountId) {
        super("Insufficient funds in account: " + accountId);
    }
}
