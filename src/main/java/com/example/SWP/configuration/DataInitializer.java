package com.example.SWP.configuration;

import com.example.SWP.entity.*;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.enums.*;
import com.example.SWP.repository.*;
import com.example.SWP.repository.wallet.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataInitializer implements CommandLineRunner {

    final SellerPackageRepository packageRepository;
    final PriorityPackageRepository priorityPackageRepository;
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final AppConfigRepository appConfigRepository;

    @Value("${seller-package.basic.price}")
    BigDecimal basicPrice_sellerPackage;

    @Value("${seller-package.basic.maxPosts}")
    int basicMaxPosts_sellerPackage;

    @Value("${seller-package.premium.price}")
    BigDecimal premiumPrice_sellerPackage;

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

    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;

    @Override
    public void run(String... args) {

        //Tao package
        if (packageRepository.count() == 0) {
            SellerPackage basic = SellerPackage.builder()
                    .type(SellerPackageType.BASIC)
                    .price(basicPrice_sellerPackage)
                    .description("Cung cấp lượt đăng bài bán cơ bản trên hệ thống.")
                    .postLimit(basicMaxPosts_sellerPackage)
                    .build();

            SellerPackage premium = SellerPackage.builder()
                    .type(SellerPackageType.PREMIUM)
                    .price(premiumPrice_sellerPackage)
                    .description("Cung cấp lượt đăng bài với đặc quyền gửi yêu cầu Admin kiểm duyệt để nhận nhãn 'Đã kiểm duyệt' để tăng độ uy tín hoặc có thể đăng thường.")
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
                    .description("Bài đăng được ưu tiên hiển thị lên đầu danh sách, giúp tiếp cận người mua nhanh hơn.")
                    .durationDays(basicDurationDays_priorityPackage)
                    .build();

            PriorityPackage premiumPriority = PriorityPackage.builder()
                    .type(PriorityPackageType.PREMIUM)
                    .price(premiumPrice_priorityPackage)
                    .description("Bài đăng được ưu tiên hiển thị lên đầu danh sách, giúp tiếp cận người mua nhanh hơn.")
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

        if (!appConfigRepository.existsByConfigKey("DEPOSIT_PERCENTAGE_KEY")) {

            AppConfig depositConfig = AppConfig.builder()
                    .configKey("DEPOSIT_PERCENTAGE_KEY")
                    .configValue(depositPercentage.toString())
                    .description("Tỉ lệ phần trăm đặt cọc bắt buộc cho các giao dịch.")
                    .build();

            appConfigRepository.save(depositConfig);
        }
    }
}