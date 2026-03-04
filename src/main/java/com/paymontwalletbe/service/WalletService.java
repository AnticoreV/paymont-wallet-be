package com.paymontwalletbe.service;

import com.paymont.wallet.api.model.Currency;
import com.paymont.wallet.api.model.WalletResponse;
import com.paymontwalletbe.mapper.WalletMapper;
import com.paymontwalletbe.model.entities.User;
import com.paymontwalletbe.model.entities.Wallet;
import com.paymontwalletbe.model.entities.enums.CurrencyType;
import com.paymontwalletbe.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final CurrentUserService currentUserService;

    @Transactional
    public WalletResponse createWallet(Currency currencyApi) {

        User user = currentUserService.getCurrentUser();

        CurrencyType currency = CurrencyType.valueOf(currencyApi.getValue());

        walletRepository.findByUserAndCurrency(user, currency)
                .ifPresent(w -> {
                    throw new IllegalStateException("Wallet already exists for this currency");
                });

        Wallet wallet = Wallet.builder()
                .user(user)
                .currency(currency)
                .createdAt(Instant.now())
                .build();

        walletRepository.save(wallet);

        return walletMapper.toResponse(wallet);
    }
}
