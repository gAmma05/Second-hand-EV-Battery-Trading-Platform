package com.example.SWP.service.seller;

import com.example.SWP.entity.*;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.*;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SellerPaymentService {

    SellerPackagePaymentRepository packagePaymentRepository;
    UserRepository userRepository;
    SellerPackageRepository packageRepository;
    WalletRepository walletRepository;
    WalletTransactionRepository walletTransactionRepository;
    PriorityPackageRepository priorityPackageRepository;
    PriorityPackagePaymentRepository priorityPackagePaymentRepository;

    @Transactional
    public void sellerPackagePayment(String email, Long packageId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        SellerPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException("Package not found", 404));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Wallet not found", 404));

        BigDecimal amount = pkg.getPrice();

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient wallet balance", 400);
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Tạo SellerPackagePayment
        SellerPackagePayment payment = SellerPackagePayment.builder()
                .user(user)
                .sellerPackage(pkg)
                .orderId("WL-" + System.currentTimeMillis())
                .build();
        packagePaymentRepository.save(payment);

        // Cập nhật gói seller
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = user.getPlanExpiry();
        boolean isStillActive = expiry != null && expiry.isAfter(now);
        LocalDateTime newExpiry;

        if (isStillActive) {
            newExpiry = expiry.plusDays(pkg.getDurationDays());
            user.setRemainingPosts(user.getRemainingPosts() + pkg.getPostLimit());
        } else {
            newExpiry = now.plusDays(pkg.getDurationDays());
            user.setRemainingPosts(pkg.getPostLimit());
        }

        user.setSellerPackageId(pkg.getId());
        user.setPlanExpiry(newExpiry);
        userRepository.save(user);

        // Lưu WalletTransaction chi tiết
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .orderId(payment.getOrderId())
                .amount(amount.negate()) // tiền ra
                .description("Purchase seller package " + pkg.getType())
                .type(TransactionType.PURCHASE_PACKAGE)
                .status(PaymentStatus.SUCCESS)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .createdAt(now)
                .updatedAt(now)
                .build();
        walletTransactionRepository.save(transaction);
    }

    @Transactional
    public PriorityPackagePayment priorityPackagePayment(User user, Long priorityPackageId) {
        // Lấy gói ưu tiên
        PriorityPackage priorityPackage = priorityPackageRepository.findById(priorityPackageId)
                .orElseThrow(() -> new BusinessException("Priority package not found", 404));

        // Lấy ví của user
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Wallet not found", 404));

        BigDecimal price = priorityPackage.getPrice();

        // Kiểm tra số dư
        if (wallet.getBalance().compareTo(price) < 0) {
            throw new BusinessException("Not enough balance in wallet", 400);
        }

        LocalDateTime now = LocalDateTime.now();

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(price);

        // Trừ tiền
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Tạo WalletTransaction
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .orderId("PP-" + System.currentTimeMillis())
                .amount(price.negate())
                .description("Purchase priority package: " + priorityPackage.getType())
                .type(TransactionType.PURCHASE_PACKAGE)
                .status(PaymentStatus.SUCCESS)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .createdAt(now)
                .updatedAt(now)
                .build();
        walletTransactionRepository.save(transaction);

        // Tạo PriorityPackagePayment, post = null tạm thời
        PriorityPackagePayment payment = PriorityPackagePayment.builder()
                .priorityPackage(priorityPackage)
                .orderId(transaction.getOrderId())
                .build();
        priorityPackagePaymentRepository.save(payment);

        return payment;
    }
}
