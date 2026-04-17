package com.smallbankapp.banking.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a registered user.
 * Pure POJO — no JPA annotations here (infrastructure concern).
 */
public class User {

    private final UUID id;
    private final String email;
    private String passwordHash;
    private final String fullName;
    private final Instant createdAt;
    private Instant updatedAt;

    public User(UUID id, String email, String passwordHash, String fullName,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User create(String email, String passwordHash, String fullName) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), email, passwordHash, fullName, now, now);
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
