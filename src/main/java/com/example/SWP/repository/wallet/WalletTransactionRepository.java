package com.example.SWP.repository.wallet;

import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByOrderId(String orderId);

    Page<WalletTransaction> findByWallet(Wallet wallet, Pageable pageable);

    Page<WalletTransaction> findByWalletAndType(Wallet wallet, TransactionType type, Pageable pageable);
}
