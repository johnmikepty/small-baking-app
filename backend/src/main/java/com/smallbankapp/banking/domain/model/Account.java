package com.smallbankapp.banking.domain.model;

import com.smallbankapp.banking.domain.exception.InsufficientFundsException;
import com.smallbankapp.banking.domain.valueobject.AccountNumber;
import com.smallbankapp.banking.domain.valueobject.AccountStatus;
import com.smallbankapp.banking.domain.valueobject.AccountType;
import com.smallbankapp.banking.domain.valueobject.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a bank account.
 * Contains business rules for balance operations.
 */
public class Account {

    private final UUID id;
    private final UUID userId;
    private final AccountNumber accountNumber;
    private final AccountType accountType;
    private Money balance;
    private AccountStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Account(UUID id, UUID userId, AccountNumber accountNumber, AccountType accountType,
                   Money balance, AccountStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account create(UUID userId, AccountType accountType, String currency) {
        Instant now = Instant.now();
        return new Account(
                UUID.randomUUID(),
                userId,
                AccountNumber.generate(),
                accountType,
                Money.zero(currency),
                AccountStatus.ACTIVE,
                now,
                now
        );
    }

    // ── Business rules ──────────────────────────────────────

    public void deposit(Money amount) {
        assertActive();
        this.balance = this.balance.add(amount);
        this.updatedAt = Instant.now();
    }

    public void withdraw(Money amount) {
        assertActive();
        if (!this.balance.isGreaterThanOrEqualTo(amount)) {
            throw new InsufficientFundsException(this.id.toString());
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    private void assertActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active: " + this.id);
        }
    }

    // ── Getters ─────────────────────────────────────────────

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public AccountNumber getAccountNumber() { return accountNumber; }
    public AccountType getAccountType() { return accountType; }
    public Money getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
