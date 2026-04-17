package com.smallbankapp.banking.domain.valueobject;

import java.util.Objects;
import java.util.Random;

/**
 * Value Object representing a unique bank account number (10 digits).
 */
public final class AccountNumber {

    private final String value;

    private AccountNumber(String value) {
        if (value == null || !value.matches("\\d{10}")) {
            throw new IllegalArgumentException("Account number must be exactly 10 digits");
        }
        this.value = value;
    }

    public static AccountNumber of(String value) {
        return new AccountNumber(value);
    }

    public static AccountNumber generate() {
        long number = (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return new AccountNumber(String.valueOf(number));
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountNumber a)) return false;
        return value.equals(a.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
