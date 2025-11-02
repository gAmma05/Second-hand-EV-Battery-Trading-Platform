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
import com.example.SWP.service.ghn.GhnService;
import com.example.SWP.service.validate.ValidateService;
import jakarta.validation.Valid;
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
    GhnService ghnService;
    ValidateService validateService;

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
        User user = validateService.validateCurrentUser(authentication);
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUserProfile(Authentication authentication, UpdateUserRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getStreetAddress() != null &&
                request.getProvinceId() != null &&
                request.getDistrictId() != null &&
                request.getWardCode() != null
        ) {

            ghnService.validateAddressIds(
                    request.getProvinceId(),
                    request.getDistrictId(),
                    request.getWardCode()
            );

            user.setProvinceId(request.getProvinceId());
            user.setDistrictId(request.getDistrictId());
            user.setWardCode(request.getWardCode());
            user.setStreetAddress(request.getStreetAddress());
            user.setAddress(ghnService.getFullAddress(request.getStreetAddress(), request.getProvinceId(), request.getDistrictId(), request.getWardCode()));
        }

        if (user.getRole().equals(Role.SELLER)) {
            if (request.getStoreName() != null) {
                user.setStoreName(request.getStoreName());
            }
            if (request.getStoreDescription() != null) {
                user.setStoreDescription(request.getStoreDescription());
            }
            if (request.getSocialMedia() != null) {
                user.setSocialMedia(request.getSocialMedia());
            }
        }

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }


    public UserResponse updateAvatar(Authentication authentication, String avatarUrl) {
        User user = validateService.validateCurrentUser(authentication);

        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }


    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Password mới và xác nhận password mới không trùng khớp", 400);
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Password cũ sai", 400);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}

