package com.example.SWP.entity.wallet;

import com.example.SWP.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<WalletTransaction> transactions;
}
