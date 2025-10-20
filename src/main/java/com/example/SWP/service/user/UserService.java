package com.example.SWP.service.user;

import com.example.SWP.dto.request.auth.CreateUserRequest;
import com.example.SWP.dto.request.user.ChangePasswordRequest;
import com.example.SWP.dto.request.user.UpdateUserRequest;

import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.entity.User;
import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.UserMapper;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void createUser(CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.BUYER)
                .fullName(request.getFullName())
                .provider(AuthProvider.MANUAL)
                .status(true)
                .build();
        userRepository.save(user);
    }

    public UserResponse getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("User is not authenticated", 401);
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new BusinessException("User is not found", 404);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUserProfile(Authentication authentication, UpdateUserRequest request) {
        String email = authentication.getName();
        User user = findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateAvatar(Authentication authentication, String avatarUrl) {
        String email = authentication.getName();
        User user = findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatar(avatarUrl);
            userRepository.save(user);
        }

        return userMapper.toUserResponse(user);
    }


    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect", 400);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }


}

