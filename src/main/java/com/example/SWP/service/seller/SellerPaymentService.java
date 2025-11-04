package com.example.SWP.service.seller;

import com.example.SWP.entity.*;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.SellerPackageType;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.*;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.utils.Utils;
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
    PriorityPackageRepository priorityPackageRepository;
    PriorityPackagePaymentRepository priorityPackagePaymentRepository;
    WalletService walletService;

    @Transactional
    public void sellerPackagePayment(String email, Long packageId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        SellerPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy gói người bán", 404));

        BigDecimal amount = pkg.getPrice();

        String code = Utils.generateCode("SELLER_PACKAGE_PAYMENT");

        walletService.payWithWallet(
                user,
                amount,
                code,
                Utils.generatePaymentDescription(TransactionType.PACKAGE, code),
                TransactionType.PACKAGE
        );

        // Tạo SellerPackagePayment
        SellerPackagePayment payment = SellerPackagePayment.builder()
                .user(user)
                .sellerPackage(pkg)
                .build();
        packagePaymentRepository.save(payment);

        // Cập nhật số lượng đăng bài của user
        if (pkg.getType() == SellerPackageType.BASIC) {
            user.setRemainingBasicPosts(user.getRemainingBasicPosts() + pkg.getPostLimit());
        } else if (pkg.getType() == SellerPackageType.PREMIUM) {
            user.setRemainingPremiumPosts(user.getRemainingPremiumPosts() + pkg.getPostLimit());
        }

        userRepository.save(user);
    }

    @Transactional
    public PriorityPackagePayment priorityPackagePayment(User user, Long priorityPackageId) {
        // Lấy gói ưu tiên
        PriorityPackage priorityPackage = priorityPackageRepository.findById(priorityPackageId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy package ưu tiên", 404));

        BigDecimal amount = priorityPackage.getPrice();
        String code = Utils.generateCode("PPP");
        walletService.payWithWallet(
                user,
                amount,
                code,
                Utils.generatePaymentDescription(TransactionType.PACKAGE, code),
                TransactionType.PACKAGE
        );

        // Tạo PriorityPackagePayment, post = null tạm thời do chua tao
        PriorityPackagePayment payment = PriorityPackagePayment.builder()
                .priorityPackage(priorityPackage)
                .build();

        priorityPackagePaymentRepository.save(payment);

        return payment;
    }
}
