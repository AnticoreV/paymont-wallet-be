package com.paymontwalletbe.model.entities;

import com.paymontwalletbe.model.entities.enums.TransactionStatus;
import com.paymontwalletbe.model.entities.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "transaction_type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "transaction_status", nullable = false)
    private TransactionStatus status;

    private String description;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
