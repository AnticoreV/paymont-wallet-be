package com.paymontwalletbe.repository;

import com.paymontwalletbe.model.entities.WalletSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// For the future functionality to fast summarize the balance
public interface WalletSnapshotRepository extends JpaRepository<WalletSnapshot, UUID> {

    Optional<WalletSnapshot> findByWalletId(UUID walletId);

}
