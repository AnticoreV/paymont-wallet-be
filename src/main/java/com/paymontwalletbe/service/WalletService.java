package com.paymontwalletbe.service;

import com.paymont.wallet.api.model.*;
import com.paymontwalletbe.mapper.TransactionMapper;
import com.paymontwalletbe.mapper.WalletMapper;
import com.paymontwalletbe.model.entities.*;
import com.paymontwalletbe.model.entities.enums.CurrencyType;
import com.paymontwalletbe.model.entities.enums.TransactionStatus;
import com.paymontwalletbe.model.entities.enums.TransactionType;
import com.paymontwalletbe.repository.TransactionEntryRepository;
import com.paymontwalletbe.repository.TransactionRepository;
import com.paymontwalletbe.repository.WalletRepository;
import com.paymontwalletbe.repository.WalletSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final TransactionRepository transactionRepository;
    private final WalletSnapshotRepository walletSnapshotRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;
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

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID walletId) {
        User currentUser = currentUserService.getCurrentUser();

        Wallet wallet = walletRepository
                .findByIdAndUserId(walletId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Currently always null, snapshot logic not implemented yet
        WalletSnapshot snapshot = walletSnapshotRepository.findByWalletId(walletId)
                .orElse(null);

        BigDecimal balance;

        if (snapshot != null) {
            BigDecimal delta = transactionEntryRepository.sumSinceEntry(walletId, snapshot.getLastEntryId());
            balance = snapshot.getBalance().add(delta);
        } else {
            balance = transactionEntryRepository.calculateBalance(walletId);
        }

        return walletMapper.toBalanceResponse(wallet, balance);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(UUID walletId) {

        User currentUser = currentUserService.getCurrentUser();

        return transactionEntryRepository
                .findEntriesWithTransaction(walletId, currentUser.getId())
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse topUp(UUID walletId, TopUpRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Wallet wallet = walletRepository
                .findByIdAndUserIdForUpdate(walletId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Instant now = Instant.now();

        Transaction transaction = Transaction.builder()
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .createdAt(now)
                .completedAt(now)
                .build();

        transactionRepository.save(transaction);

        TransactionEntry entry = TransactionEntry.builder()
                .transaction(transaction)
                .wallet(wallet)
                .amount(amount)
                .createdAt(now)
                .build();

        transactionEntryRepository.save(entry);

        return transactionMapper.toResponse(entry);
    }

    @Transactional
    public TransactionResponse withdraw(UUID walletId, WithdrawRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Wallet wallet = walletRepository
                .findByIdAndUserIdForUpdate(walletId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal currentBalance =
                transactionEntryRepository.calculateBalance(walletId);

        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        Instant now = Instant.now();

        Transaction transaction = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .referenceNumber(request.getTargetAccount())
                .createdAt(now)
                .completedAt(now)
                .build();

        transactionRepository.save(transaction);

        TransactionEntry entry = TransactionEntry.builder()
                .transaction(transaction)
                .wallet(wallet)
                .amount(amount.negate())
                .createdAt(now)
                .build();

        transactionEntryRepository.save(entry);

        return transactionMapper.toResponse(entry);
    }
}
