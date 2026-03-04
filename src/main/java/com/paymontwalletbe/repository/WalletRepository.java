package com.paymontwalletbe.repository;

import com.paymontwalletbe.model.entities.User;
import com.paymontwalletbe.model.entities.Wallet;
import com.paymontwalletbe.model.entities.enums.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserAndCurrency(User user, CurrencyType currency);
}
