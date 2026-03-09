package com.paymontwalletbe.repository;

import com.paymontwalletbe.model.entities.User;
import com.paymontwalletbe.model.entities.Wallet;
import com.paymontwalletbe.model.entities.enums.CurrencyType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserAndCurrency(User user, CurrencyType currency);
    Optional<Wallet> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT w
    FROM Wallet w
    WHERE w.id = :walletId
    AND w.user.id = :userId
""")
    Optional<Wallet> findByIdAndUserIdForUpdate(UUID walletId, UUID userId);

    List<Wallet> findAllByUserId(UUID userId);
}
