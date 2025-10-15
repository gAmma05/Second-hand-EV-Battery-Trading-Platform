package com.example.SWP.repository.wallet;

import com.example.SWP.entity.wallet.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByOrderId(String orderId);
}
