package com.paymontwalletbe.controllers;

import com.paymont.wallet.api.WalletsApi;
import com.paymont.wallet.api.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class WalletController implements WalletsApi {

    @Override
    public ResponseEntity<WalletResponse> createWallet(CreateWalletRequest createWalletRequest) {
        return WalletsApi.super.createWallet(createWalletRequest);
    }

    @Override
    public ResponseEntity<BalanceResponse> getBalance(UUID walletId) {
        return WalletsApi.super.getBalance(walletId);
    }

    @Override
    public ResponseEntity<List<TransactionResponse>> getTransactions(UUID walletId) {
        return WalletsApi.super.getTransactions(walletId);
    }

    @Override
    public ResponseEntity<TransactionResponse> topUp(UUID walletId, TopUpRequest topUpRequest) {
        return WalletsApi.super.topUp(walletId, topUpRequest);
    }

    @Override
    public ResponseEntity<TransactionResponse> withdraw(UUID walletId, WithdrawRequest withdrawRequest) {
        return WalletsApi.super.withdraw(walletId, withdrawRequest);
    }
}
