-- ============================================================
-- V1__init_schema.sql
-- Initial schema: users, accounts, transactions
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (email);

-- ── Accounts ────────────────────────────────────────────────
CREATE TABLE accounts (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type   VARCHAR(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'CHECKING')),
    balance        NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    currency       VARCHAR(3)  NOT NULL DEFAULT 'USD',
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_account_number ON accounts (account_number);

-- ── Transactions ─────────────────────────────────────────────
CREATE TABLE transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     UUID NOT NULL UNIQUE,
    source_account_id   UUID REFERENCES accounts(id),
    target_account_id   UUID REFERENCES accounts(id),
    transaction_type    VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    amount              NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency            VARCHAR(3)  NOT NULL DEFAULT 'USD',
    description         VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED', 'PENDING', 'FAILED')),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_source ON transactions (source_account_id);
CREATE INDEX idx_transactions_target ON transactions (target_account_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
CREATE INDEX idx_transactions_idempotency ON transactions (idempotency_key);
