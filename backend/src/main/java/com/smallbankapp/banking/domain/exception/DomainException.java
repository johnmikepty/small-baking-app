package com.smallbankapp.banking.domain.exception;

/**
 * Base class for all domain-level exceptions.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
