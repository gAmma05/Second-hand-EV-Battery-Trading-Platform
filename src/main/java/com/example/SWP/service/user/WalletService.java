package com.example.SWP.service.user;

import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.User;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PriorityPackageRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import com.example.SWP.service.payment.VnPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    UserRepository userRepository;
    VnPayService vnPayService;
    PriorityPackageRepository priorityPackageRepository;

    @NonFinal
    @Value("${vnpay.returnUrl.walletDeposit}")
    String walletReturnUrl;

    //Xu ly nap tien vao vi
    public String deposit(String email, BigDecimal amount) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .user(user)
                                .balance(BigDecimal.ZERO)
                                .build()
                ));

        String orderId = String.valueOf(System.currentTimeMillis());
        String description = "Deposit to wallet";

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

        return vnPayService.createPaymentUrl(orderId, amount.intValue(), description, walletReturnUrl);
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


    public WalletTransaction withdraw(String email, BigDecimal amount, String bankAccount) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        Wallet wallet = user.getWallet();
        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for withdrawal", 400);
        }

        // Trừ tiền khỏi ví
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Tạo giao dịch rút tiền
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.WITHDRAW)
                .status(PaymentStatus.SUCCESS)
                .bankCode(bankAccount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();

        return walletTransactionRepository.save(transaction);
    }

    public BigDecimal getBalance(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            return BigDecimal.ZERO;
        }
        return wallet.getBalance();
    }

    //Lich su giao dich cua wallet
    public List<WalletTransaction> getTransactions(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WalletTransaction> transactionsPage = walletTransactionRepository.findByWallet(user.getWallet(), pageable);

        return transactionsPage.getContent();
    }

    //Xem chi tiet 1 giao dich trong lich su wallet
    public WalletTransaction getTransactionDetail(String email, Long transactionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        WalletTransaction transaction = walletTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found", 404));

        // Kiểm tra giao dịch có thuộc ví của user không
        if (!transaction.getWallet().getId().equals(user.getWallet().getId())) {
            throw new BusinessException("You are not authorized to view this transaction", 403);
        }

        return transaction;
    }

    // Trừ tiền trong wallet khi mua goi uu tien
    public void payPriorityPackage(User user, Long priorityPackageId) {
        PriorityPackage priorityPackage = priorityPackageRepository.findById(priorityPackageId)
                .orElseThrow(() -> new BusinessException("Priority package not found", 404));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Wallet not found", 404));

        BigDecimal price = priorityPackage.getPrice();

        if (wallet.getBalance().compareTo(price) < 0) {
            throw new BusinessException("Not enough balance in wallet", 400);
        }

        wallet.setBalance(wallet.getBalance().subtract(price));
        walletRepository.save(wallet);
    }
}
