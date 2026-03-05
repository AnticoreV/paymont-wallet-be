CREATE TABLE wallet_snapshots
(
    id            UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    wallet_id     UUID           NOT NULL UNIQUE,
    balance       DECIMAL(19, 4) NOT NULL,
    last_entry_id UUID,
    created_at    TIMESTAMP      NOT NULL DEFAULT now(),
    CONSTRAINT fk_wallet_snapshot FOREIGN KEY (wallet_id) REFERENCES wallets (id)
);

CREATE INDEX idx_snapshot_wallet ON wallet_snapshots (wallet_id);

CREATE INDEX idx_tx_entry_wallet_created
    ON transaction_entries (wallet_id, created_at DESC);
