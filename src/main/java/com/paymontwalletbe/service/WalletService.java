package com.paymontwalletbe.service;

import com.paymont.wallet.api.model.*;
import com.paymontwalletbe.exception.BadRequestException;
import com.paymontwalletbe.exception.InsufficientFundsException;
import com.paymontwalletbe.exception.WalletAlreadyExistsException;
import com.paymontwalletbe.exception.WalletNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
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
        log.debug("Create wallet requested: userId={} currency={}", user.getId(), currencyApi);

        CurrencyType currency = CurrencyType.valueOf(currencyApi.getValue());

        walletRepository.findByUserAndCurrency(user, currency)
                .ifPresent(w -> {
                    log.warn(
                            "Wallet creation rejected: already exists userId={} currency={} walletId={}",
                            user.getId(),
                            currency,
                            w.getId()
                    );
                    throw new WalletAlreadyExistsException(
                            "Wallet already exists for currency: " + currency
                    );
                });

        Wallet wallet = Wallet.builder()
                .user(user)
                .currency(currency)
                .createdAt(Instant.now())
                .build();

        walletRepository.save(wallet);
        log.info(
                "Wallet created: walletId={} userId={} currency={}",
                wallet.getId(),
                user.getId(),
                currency
        );

        return walletMapper.toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {

        User currentUser = currentUserService.getCurrentUser();
        log.debug("Get wallet requested: walletId={} userId={}", walletId, currentUser.getId());

        Wallet wallet = walletRepository
                .findByIdAndUserId(walletId, currentUser.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        return walletMapper.toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getWallets() {

        User currentUser = currentUserService.getCurrentUser();
        log.debug("List wallets requested: userId={}", currentUser.getId());

        return walletRepository.findAllByUserId(currentUser.getId())
                .stream()
                .map(walletMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID walletId) {
        User currentUser = currentUserService.getCurrentUser();
        log.debug("Get balance requested: walletId={} userId={}", walletId, currentUser.getId());

        Wallet wallet = walletRepository
                .findByIdAndUserId(walletId, currentUser.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

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

        log.debug("Balance fetched: walletId={} userId={} balance={}", walletId, currentUser.getId(), balance);

        return walletMapper.toBalanceResponse(wallet, balance);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(UUID walletId) {

        User currentUser = currentUserService.getCurrentUser();
        log.debug("List transactions requested: walletId={} userId={}", walletId, currentUser.getId());

        return transactionEntryRepository
                .findEntriesWithTransaction(walletId, currentUser.getId())
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse topUp(UUID walletId, TopUpRequest request) {

        User currentUser = currentUserService.getCurrentUser();
        log.debug(
                "Top-up requested: walletId={} userId={} amount={}",
                walletId,
                currentUser.getId(),
                request.getAmount()
        );

        Wallet wallet = walletRepository
                .findByIdAndUserIdForUpdate(walletId, currentUser.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(
                    "Top-up rejected: non-positive amount walletId={} userId={} amount={}",
                    walletId,
                    currentUser.getId(),
                    amount
            );
            throw new BadRequestException("Amount must be positive");
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
        log.info(
                "Top-up completed: walletId={} userId={} amount={} currency={}",
                walletId,
                currentUser.getId(),
                amount,
                wallet.getCurrency()
        );

        return transactionMapper.toResponse(entry);
    }

    @Transactional
    public TransactionResponse withdraw(UUID walletId, WithdrawRequest request) {

        User currentUser = currentUserService.getCurrentUser();
        log.debug(
                "Withdrawal requested: walletId={} userId={} amount={} targetAccount={}",
                walletId,
                currentUser.getId(),
                request.getAmount(),
                request.getTargetAccount()
        );

        Wallet wallet = walletRepository
                .findByIdAndUserIdForUpdate(walletId, currentUser.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(
                    "Withdrawal rejected: non-positive amount walletId={} userId={} amount={}",
                    walletId,
                    currentUser.getId(),
                    amount
            );
            throw new BadRequestException("Amount must be positive");
        }

        BigDecimal currentBalance = transactionEntryRepository.calculateBalance(walletId);

        if (currentBalance.compareTo(amount) < 0) {
            log.warn(
                    "Withdrawal rejected: insufficient funds walletId={} userId={} requested={} balance={}",
                    walletId,
                    currentUser.getId(),
                    amount,
                    currentBalance
            );
            throw new InsufficientFundsException("Insufficient funds");
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
        log.info(
                "Withdrawal completed: walletId={} userId={} amount={} currency={}",
                walletId,
                currentUser.getId(),
                amount,
                wallet.getCurrency()
        );

        return transactionMapper.toResponse(entry);
    }
}
