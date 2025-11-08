package com.example.SWP.service.user;

import com.example.SWP.entity.User;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import com.example.SWP.service.notification.NotificationService;
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
    NotificationService notificationService;

    @NonFinal
    @Value("${vnpay.returnUrl.walletDeposit}")
    String walletReturnUrl;

    public boolean checkUserWaller(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);
        return walletRepository.existsByUser(user);
    }

    public void createWallet(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        if(walletRepository.existsByUser(user)) {
            throw new BusinessException("Người dùng đã có ví, không thể tạo thêm", 400);
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();

        walletRepository.save(wallet);

        notificationService.sendNotificationToOneUser(
                user.getEmail(),
                "Tạo ví thành công",
                "Ví của bạn đã được tạo thành công với số dư ban đầu là 0 VNĐ."
        );
    }


    //Xu ly nap tien vao vi
    public String toUp(Authentication authentication, BigDecimal amount) {
        User user = validateService.validateCurrentUser(authentication);

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ví của người dùng", 404));

        String orderId = Utils.generateCode(TransactionType.TOUP_WALLET.name());
        String description = Utils.generatePaymentDescription(TransactionType.TOUP_WALLET, orderId);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .orderId(orderId)
                .amount(amount)
                .description(description)
                .type(TransactionType.TOUP_WALLET)
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
                .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch", 404));

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

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ví của người dùng", 404));

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException("Không đủ tiền để rút theo yêu cầu", 400);
        }

        // Trừ tiền khỏi ví
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        String orderId = Utils.generateCode(TransactionType.WITHDRAW_WALLET.name());
        String description = Utils.generatePaymentDescription(TransactionType.WITHDRAW_WALLET, orderId);

        // Tạo giao dịch rút tiền
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.WITHDRAW_WALLET)
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

    public void refundToWallet(User user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ví của người dùng", 404));

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        String orderId = Utils.generateCode(TransactionType.REFUND_WALLET.name());
        String description = Utils.generatePaymentDescription(TransactionType.REFUND_WALLET, orderId);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .orderId(orderId)
                .amount(amount)
                .type(TransactionType.REFUND_WALLET)
                .status(PaymentStatus.SUCCESS)
                .description(description)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();

        walletTransactionRepository.save(transaction);
    }


    //Xem so du vi
    public BigDecimal getBalance(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ví của người dùng", 404));

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
                .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch", 404));

        // Kiểm tra giao dịch có thuộc ví của user không
        if (!transaction.getWallet().getId().equals(user.getWallet().getId())) {
            throw new BusinessException("Bạn không có quyền xem ví nào ngoài của mình", 403);
        }

        return transaction;
    }

    //Thanh toan bang vi
    public void payWithWallet(
            User user, BigDecimal amount, String orderId, String description, TransactionType transactionType
    ) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Ví không có số dư", 404));

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException("Ví không đủ số dư", 400);
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
