package com.paymontwalletbe.controllers;

import com.paymont.wallet.api.WalletsApi;
import com.paymont.wallet.api.model.*;
import com.paymontwalletbe.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class WalletController implements WalletsApi {

    private final WalletService walletService;

    @Override
    public ResponseEntity<WalletResponse> createWallet(CreateWalletRequest request) {

        WalletResponse response =
                walletService.createWallet(request.getCurrency());

        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<WalletResponse> getWallet(UUID walletId) {
        return ResponseEntity.ok(walletService.getWallet(walletId));
    }

    @Override
    public ResponseEntity<List<WalletResponse>> getWallets() {
        return ResponseEntity.ok(walletService.getWallets());
    }

    @Override
    public ResponseEntity<BalanceResponse> getBalance(UUID walletId) {
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }

    @Override
    public ResponseEntity<List<TransactionResponse>> getTransactions(UUID walletId) {
        return ResponseEntity.ok(walletService.getTransactions(walletId));
    }

    @Override
    public ResponseEntity<TransactionResponse> topUp(UUID walletId, TopUpRequest request) {
        return ResponseEntity.ok(walletService.topUp(walletId, request));
    }

    @Override
    public ResponseEntity<TransactionResponse> withdraw(UUID walletId, WithdrawRequest request) {
        return ResponseEntity.ok(walletService.withdraw(walletId, request));
    }
}
