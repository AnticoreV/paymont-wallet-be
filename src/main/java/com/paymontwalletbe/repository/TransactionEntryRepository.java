package com.paymontwalletbe.repository;

import com.paymontwalletbe.model.entities.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, UUID> {
    @Query("""
                SELECT COALESCE(SUM(e.amount), 0)
                FROM TransactionEntry e
                WHERE e.wallet.id = :walletId
            """)
    BigDecimal calculateBalance(UUID walletId);

    List<TransactionEntry> findAllByWalletIdAndWalletUserIdOrderByCreatedAtDesc(
            UUID walletId,
            UUID userId
    );
}
