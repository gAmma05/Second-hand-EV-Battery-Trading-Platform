package com.example.SWP.repository.wallet;

import com.example.SWP.entity.User;
import com.example.SWP.entity.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);

    boolean existsByUser(User user);

    boolean existsByUser_Email(String userEmail);
}
