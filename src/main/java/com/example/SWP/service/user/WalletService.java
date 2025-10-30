package com.example.SWP.service.user;

import com.example.SWP.entity.User;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import com.example.SWP.service.payment.VnPayService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.utils.Utils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService {

    WalletRepository walletRepository;
    WalletTransactionRepository walletTransactionRepository;
    VnPayService vnPayService;
    ValidateService validateService;

    @NonFinal
    @Value("${vnpay.returnUrl.walletDeposit}")
    String walletReturnUrl;

    //Xu ly nap tien vao vi
    public String deposit(Authentication authentication, BigDecimal amount) {
        User user = validateService.validateCurrentUser(authentication);

        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .user(user)
                                .balance(BigDecimal.ZERO)
                                .build()
                ));

        String orderId = Utils.generateCode("DEPOSIT");
        String description = Utils.generatePaymentDescription(TransactionType.DEPOSIT, orderId);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .orderId(orderId)
                .amount(amount)
                .description(description)
                .type(TransactionType.DEPOSIT)
                .status(PaymentStatus.PENDING)
                .balanceBefore(wallet.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        walletTransactionRepository.save(transaction);

        return vnPayService.createPaymentUrl(orderId, amount, description, walletReturnUrl);
    }

    //Xu ly khi VNPay callback goi ve
    public WalletTransaction handleDepositVNPayReturn(String orderId, String responseCode, String bankCode) {
        WalletTransaction transaction = walletTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Transaction not found", 404));

        Wallet wallet = transaction.getWallet();

        if ("00".equals(responseCode)) {
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setBankCode(bankCode);

            BigDecimal newBalance = wallet.getBalance().add(transaction.getAmount());
            transaction.setBalanceAfter(newBalance);

            wallet.setBalance(newBalance);
            walletRepository.save(wallet);
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
        }

        transaction.setUpdatedAt(LocalDateTime.now());
        return walletTransactionRepository.save(transaction);
    }


    //Xu li rut tien
    public WalletTransaction withdraw(Authentication authentication, BigDecimal amount, String bankAccount) {
        User user = validateService.validateCurrentUser(authentication);

        Wallet wallet = user.getWallet();
        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for withdrawal", 400);
        }

        // Trừ tiền khỏi ví
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        String orderId = Utils.generateCode("WITHDRAW");
        String description = Utils.generatePaymentDescription(TransactionType.WITHDRAW, orderId);

        // Tạo giao dịch rút tiền
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.WITHDRAW)
                .status(PaymentStatus.SUCCESS)
                .orderId(orderId)
                .description(description)
                .bankCode(bankAccount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();

        return walletTransactionRepository.save(transaction);
    }

    //Xem so du vi
    public BigDecimal getBalance(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        Wallet wallet = user.getWallet();

        if (wallet == null) {
            return BigDecimal.ZERO;
        }

        return wallet.getBalance();
    }

    //Lich su giao dich cua wallet
    public List<WalletTransaction> getTransactions(
            Authentication authentication, int page, int size
    ) {
        User user = validateService.validateCurrentUser(authentication);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<WalletTransaction> transactionsPage = walletTransactionRepository.findByWallet(user.getWallet(), pageable);

        return transactionsPage.getContent();
    }

    //Loc lich su giao dich theo loai giao dich
    public List<WalletTransaction>  getTransactionsByType(
            Authentication authentication, TransactionType type, int page, int size
    ) {
        User user = validateService.validateCurrentUser(authentication);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<WalletTransaction> transactionsPage = walletTransactionRepository.findByWalletAndType(user.getWallet(), type, pageable);

        return transactionsPage.getContent();
    }

    //Xem chi tiet 1 giao dich trong lich su wallet
    public WalletTransaction getTransactionDetail(
            Authentication authentication, Long transactionId
    ) {
        User user = validateService.validateCurrentUser(authentication);

        WalletTransaction transaction = walletTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found", 404));

        // Kiểm tra giao dịch có thuộc ví của user không
        if (!transaction.getWallet().getId().equals(user.getWallet().getId())) {
            throw new BusinessException("You are not authorized to view this transaction", 403);
        }

        return transaction;
    }

    //Thanh toan bang vi
    public void payWithWallet(
            User user, BigDecimal amount, String orderId, String description, TransactionType transactionType
    ) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Wallet has no balance", 404));

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance in wallet", 400);
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .orderId(orderId)
                .amount(amount)
                .type(transactionType)
                .status(PaymentStatus.SUCCESS)
                .description(description)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();

        walletTransactionRepository.save(transaction);
    }

    public List<WalletTransaction> getTransactionsByStatus(
            Authentication authentication,
            PaymentStatus status,
            int page, int size)
    {
        User user = validateService.validateCurrentUser(authentication);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<WalletTransaction> transactionsPage = walletTransactionRepository.findByWalletAndStatus(user.getWallet(), status, pageable);

        return transactionsPage.getContent();
    }
}
