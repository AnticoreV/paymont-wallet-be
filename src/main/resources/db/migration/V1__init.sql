CREATE TYPE currency_type AS ENUM ('EUR', 'CZK');

CREATE TYPE transaction_type AS ENUM (
    'TOP_UP',
    'WITHDRAWAL',
    'TRANSFER'
    );

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED'
    );

CREATE TABLE transaction_entries
(
    id             UUID                        NOT NULL,
    transaction_id UUID                        NOT NULL,
    wallet_id      UUID                        NOT NULL,
    amount         DECIMAL(19, 4)              NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_transaction_entries PRIMARY KEY (id)
);

CREATE TABLE transactions
(
    id               UUID                        NOT NULL,
    type             TRANSACTION_TYPE            NOT NULL,
    status           TRANSACTION_STATUS          NOT NULL,
    description      VARCHAR(255),
    reference_number VARCHAR(255),
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    completed_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID                        NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    full_name  VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE wallets
(
    id         UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    currency   VARCHAR(3)                  NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_wallets PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE wallets
    ADD CONSTRAINT unique_user_currency UNIQUE (user_id, currency);

ALTER TABLE transaction_entries
    ADD CONSTRAINT FK_TRANSACTION_ENTRIES_ON_TRANSACTION FOREIGN KEY (transaction_id) REFERENCES transactions (id);

ALTER TABLE transaction_entries
    ADD CONSTRAINT FK_TRANSACTION_ENTRIES_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallets (id);

ALTER TABLE wallets
    ADD CONSTRAINT FK_WALLETS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_wallet_user ON wallets(user_id);
CREATE INDEX idx_entries_wallet ON transaction_entries(wallet_id);
CREATE INDEX idx_entries_transaction ON transaction_entries(transaction_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
