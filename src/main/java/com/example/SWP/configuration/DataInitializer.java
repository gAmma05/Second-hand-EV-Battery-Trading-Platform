package com.example.SWP.configuration;

import com.example.SWP.entity.Package;
import com.example.SWP.entity.User;
import com.example.SWP.enums.*;
import com.example.SWP.repository.PackageRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataInitializer implements CommandLineRunner {

    final PackageRepository packageRepository;
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    @Value("${package.basic.price}")
    int basicPrice;

    @Value("${package.basic.durationDays}")
    int basicDurationDays;

    @Value("${package.basic.maxPosts}")
    int basicMaxPosts;

    @Value("${package.premium.price}")
    int premiumPrice;

    @Value("${package.premium.durationDays}")
    int premiumDurationDays;

    @Value("${package.premium.maxPosts}")
    int premiumMaxPosts;

    @Override
    public void run(String... args) {

        //Tao package
        if (packageRepository.count() == 0) {

            Package basic = Package.builder()
                    .planType(SellerPlan.BASIC)
                    .price(basicPrice)
                    .durationDays(basicDurationDays)
                    .postLimit(basicMaxPosts)
                    .build();

            Package premium = Package.builder()
                    .planType(SellerPlan.PREMIUM)
                    .price(premiumPrice)
                    .durationDays(premiumDurationDays)
                    .postLimit(premiumMaxPosts)
                    .build();

            packageRepository.saveAll(List.of(basic, premium));
            log.info("Packages (BASIC, PREMIUM) loaded from application.properties");
        } else {
            log.info("Packages already exist, skipping initialization");
        }

        //Tao admin
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin"))
                    .fullName("Admin")
                    .enabled(true)
                    .status(true)
                    .role(Role.ADMIN)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(admin);
            log.warn("Admin user created with email 'admin@gmail.com' and password 'admin'. Please change the password after first login.");
        }
    }
}
