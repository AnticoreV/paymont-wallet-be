package com.paymontwalletbe.model.entities;

import com.paymontwalletbe.model.entities.enums.CurrencyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_user_currency", columnNames = {"user_id", "currency"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "currency_type", nullable = false)
    private CurrencyType currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
