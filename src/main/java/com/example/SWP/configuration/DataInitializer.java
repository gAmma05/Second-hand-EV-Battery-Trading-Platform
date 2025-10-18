package com.example.SWP.configuration;

import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.entity.User;
import com.example.SWP.enums.*;
import com.example.SWP.repository.SellerPackageRepository;
import com.example.SWP.repository.PriorityPackageRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataInitializer implements CommandLineRunner {

    final SellerPackageRepository packageRepository;
    final PriorityPackageRepository priorityPackageRepository;
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    @Value("${seller-package.basic.price}")
    BigDecimal basicPrice_sellerPackage;

    @Value("${seller-package.basic.durationDays}")
    int basicDurationDays_sellerPackage;

    @Value("${seller-package.basic.maxPosts}")
    int basicMaxPosts_sellerPackage;

    @Value("${seller-package.premium.price}")
    BigDecimal premiumPrice_sellerPackage;

    @Value("${seller-package.premium.durationDays}")
    int premiumDurationDays_sellerPackage;

    @Value("${seller-package.premium.maxPosts}")
    int premiumMaxPosts_sellerPackage;

    @Value("${priority-package.basic.price}")
    BigDecimal basicPrice_priorityPackage;

    @Value("${priority-package.basic.durationDays}")
    int basicDurationDays_priorityPackage;

    @Value("${priority-package.premium.price}")
    BigDecimal premiumPrice_priorityPackage;

    @Value("${priority-package.premium.durationDays}")
    int premiumDurationDays_priorityPackage;


    @Override
    public void run(String... args) {

        //Tao package
        if (packageRepository.count() == 0) {

            SellerPackage basic = SellerPackage.builder()
                    .type(SellerPackageType.BASIC)
                    .price(basicPrice_sellerPackage)
                    .durationDays(basicDurationDays_sellerPackage)
                    .postLimit(basicMaxPosts_sellerPackage)
                    .build();

            SellerPackage premium = SellerPackage.builder()
                    .type(SellerPackageType.PREMIUM)
                    .price(premiumPrice_sellerPackage)
                    .durationDays(premiumDurationDays_sellerPackage)
                    .postLimit(premiumMaxPosts_sellerPackage)
                    .build();

            packageRepository.saveAll(List.of(basic, premium));
            log.info("Seller packages (BASIC, PREMIUM) loaded from application.properties");
        } else {
            log.info("Seller packages already exist, skipping initialization");
        }

        if (priorityPackageRepository.count() == 0) {

            PriorityPackage basicPriority = PriorityPackage.builder()
                    .type(PriorityPackageType.BASIC)
                    .price(basicPrice_priorityPackage)
                    .durationDays(basicDurationDays_priorityPackage)
                    .build();

            PriorityPackage premiumPriority = PriorityPackage.builder()
                    .type(PriorityPackageType.PREMIUM)
                    .price(premiumPrice_priorityPackage)
                    .durationDays(premiumDurationDays_priorityPackage)
                    .build();

            priorityPackageRepository.saveAll(List.of(basicPriority, premiumPriority));
            log.info("Priority packages (BASIC, PREMIUM) loaded from application.properties");
        } else {
            log.info("Priority packages already exist, skipping initialization");
        }

        //Tao admin
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin@"))
                    .fullName("Admin")
                    .status(true)
                    .role(Role.ADMIN)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(admin);
            log.warn("Admin user created with email 'admin@gmail.com' and password 'admin'. Please change the password after first login.");
        }

        //Test
        if (!userRepository.existsByRole(Role.BUYER)) {
            User user = User.builder()
                    .email("buyer@gmail.com")
                    .password(passwordEncoder.encode("buyer"))
                    .fullName("Buyer")
                    .status(true)
                    .role(Role.BUYER)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(user);
            log.warn("Buyer user created with email 'buyer@gmail.com' and password 'buyer'. Please change the password after first login.");
        }

        //Test
        if (!userRepository.existsByRole(Role.SELLER)) {
            User user = User.builder()
                    .email("seller@gmail.com")
                    .password(passwordEncoder.encode("seller"))
                    .fullName("Seller")
                    .status(true)
                    .remainingPosts(1000)
                    .role(Role.SELLER)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(user);
            log.warn("Seller user created with email 'seller@gmail.com' and password 'seller'. Please change the password after first login.");
        }
    }
}
