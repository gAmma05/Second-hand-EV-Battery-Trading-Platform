package com.example.SWP.repository.wallet;

import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByOrderId(String orderId);

    List<WalletTransaction> findByWallet(Wallet wallet);

    List<WalletTransaction> findByWalletAndType(Wallet wallet, TransactionType type);

    List<WalletTransaction> findByWalletAndStatus(Wallet wallet, PaymentStatus status);
}
