package com.smallbankapp.banking.domain.exception;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
    }
}
