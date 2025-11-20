package com.example.SWP.configuration;

import com.example.SWP.entity.User;
import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class DataInitializer implements CommandLineRunner {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("DataInitializer is running...");
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
            log.warn("Admin user created with email 'admin@gmail.com' and password 'admin@'. Please change the password after first login.");
        }

        if (!userRepository.existsByRole(Role.SELLER)) {
            User seller = User.builder()
                    .email("luatluat304@gmail.com")
                    .password(passwordEncoder.encode("seller@"))
                    .fullName("Seller")
                    .status(true)
                    .role(Role.SELLER)
                    .provider(AuthProvider.MANUAL)
                    .build();

            userRepository.save(seller);
            log.warn("Seller user created with email 'luatluat304@gmail.com' and password 'seller@'. Please change the password after first login.");
        }
    }

}
