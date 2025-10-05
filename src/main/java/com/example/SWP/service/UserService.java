package com.example.SWP.service;

import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.entity.User;
import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void createInactiveUser(RegisterRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.BUYER)
                .fullName(request.getFullName())
                .enabled(false)
                .build();
        userRepository.save(user);
    }

    public void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }
}

