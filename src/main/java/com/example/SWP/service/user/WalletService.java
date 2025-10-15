package com.example.SWP.service.user;

import com.example.SWP.entity.User;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import com.example.SWP.service.payment.VnPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService {

    WalletRepository walletRepository;
    WalletTransactionRepository walletTransactionRepository;
    UserRepository userRepository;
    VnPayService vnPayService;

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
}
