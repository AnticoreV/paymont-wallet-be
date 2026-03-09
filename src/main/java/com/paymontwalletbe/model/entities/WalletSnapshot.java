package com.paymontwalletbe.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletSnapshot {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, unique = true)
    private Wallet wallet;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "last_entry_id")
    private UUID lastEntryId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public WalletSnapshot(Wallet wallet, BigDecimal balance, UUID lastEntryId) {
        this.wallet = wallet;
        this.balance = balance;
        this.lastEntryId = lastEntryId;
        this.createdAt = Instant.now();
    }
}
