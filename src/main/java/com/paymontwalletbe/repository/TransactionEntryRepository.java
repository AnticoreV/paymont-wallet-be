package com.paymontwalletbe.repository;

import com.paymontwalletbe.model.entities.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, UUID> {
    @Query("""
                SELECT COALESCE(SUM(e.amount), 0)
                FROM TransactionEntry e
                WHERE e.wallet.id = :walletId
            """)
    BigDecimal calculateBalance(UUID walletId);

    @Query("""
                SELECT e
                FROM TransactionEntry e
                JOIN FETCH e.transaction t
                JOIN FETCH e.wallet w
                WHERE w.id = :walletId
                AND w.user.id = :userId
                ORDER BY e.createdAt DESC
            """)
    List<TransactionEntry> findEntriesWithTransaction(UUID walletId, UUID userId);

    @Query("""
                SELECT COALESCE(SUM(te.amount), 0)
                FROM TransactionEntry te
                WHERE te.wallet.id = :walletId
                  AND te.id > :lastEntryId
            """)
    BigDecimal sumSinceEntry(@Param("walletId") UUID walletId,
                             @Param("lastEntryId") UUID lastEntryId);

    @Query("""
                SELECT COALESCE(SUM(te.amount), 0)
                FROM TransactionEntry te
                WHERE te.wallet.id = :walletId
            """)
    BigDecimal sumAll(@Param("walletId") UUID walletId);

    Optional<TransactionEntry> findTopByWalletIdOrderByCreatedAtDesc(UUID walletId);

}
