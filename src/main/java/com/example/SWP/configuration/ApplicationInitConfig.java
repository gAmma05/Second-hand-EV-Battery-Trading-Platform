package com.example.SWP.configuration;

import com.example.SWP.entity.User;
import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import com.example.SWP.repository.UserRepository;
import io.micrometer.common.lang.NonNullApi;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@NonNullApi
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByRole(Role.ADMIN)) {
                User user = User.builder()
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin"))
                        .fullName("admin")
                        .enabled(true)
                        .role(Role.ADMIN)
                        .provider(AuthProvider.MANUAL)
                        .build();
                userRepository.save(user);
                log.warn("Admin user created with username 'admin' and password 'admin'. Please change the password after first login.");
            }
        };
    }
}
