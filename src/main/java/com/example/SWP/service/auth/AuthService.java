package com.example.SWP.service.auth;

import com.example.SWP.dto.request.auth.BasicLoginRequest;
import com.example.SWP.dto.request.auth.GoogleLoginRequest;
import com.example.SWP.dto.request.auth.CreateUserRequest;

import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.entity.User;
import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.OtpType;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.UserMapper;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.jwt.JwtService;
import com.example.SWP.service.mail.MailService;
import com.example.SWP.service.mail.OtpService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    GoogleClientService googleClientService;
    UserService userService;
    OtpService otpService;
    MailService mailService;
    NotificationService notificationService;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    JwtService jwtService;

    public String register(CreateUserRequest request) {
        String email = request.getEmail();

        // Nếu email đã tồn tại thi chặn đăng ký
        User existingUser = userService.findByEmail(email);
        if (existingUser != null) {
            throw new BusinessException("Email is already in use!", 400);
        }

        // Lưu thông tin đăng ký tạm thời trong Redis
        otpService.storePendingRegistration(email, request);

        // Sinh và gửi OTP
        String otp = otpService.generateAndStoreOtp(email, OtpType.REGISTER);
        mailService.sendOtpEmail(email, otp, OtpType.REGISTER);

        return "Registration initiated! Please check your email for OTP.";
    }

    public void verifyRegister(String email, String otpInput) {
        // Kiểm tra OTP
        otpService.verifyOtp(email, otpInput, OtpType.REGISTER);

        // Lấy dữ liệu đăng ký đã lưu tạm trong Redis
        CreateUserRequest pendingRequest = otpService.getPendingRegistration(email);
        if (pendingRequest == null) {
            throw new BusinessException("Registration data expired or not found", 400);
        }

        // Kiểm tra lại (đề phòng race condition)
        if (userService.findByEmail(email) != null) {
            throw new BusinessException("User already exists", 400);
        }

        // Tạo mới user
        userService.createUser(pendingRequest);
        otpService.deletePendingRegistration(email);
    }


    public Map<String, Object> basicLogin(BasicLoginRequest basicLoginRequest) {
        Optional<User> result = userRepository.findByEmail(basicLoginRequest.getEmail());

        if (result.isEmpty() ||
                !passwordEncoder.matches(basicLoginRequest.getPassword(), result.get().getPassword())) {
            throw new BusinessException("Invalid email or password", 400);
        }

        User user = result.get();
        UserResponse userResponse = userMapper.toUserResponse(user);
        String accessToken = jwtService.generateAccessToken(user);

        Map<String, Object> body = new HashMap<>();
        body.put("accessToken", accessToken);
        body.put("user", userResponse);

        return body;
    }


    //GOOGLE (Dung)
    @Transactional
    public Map<String, Object> processGoogleToken(GoogleLoginRequest googleLoginRequest) {
        Map<String, Object> body = new HashMap<>();

        GoogleIdToken.Payload payload = googleClientService.verifyGoogleIdToken(googleLoginRequest);

        if (payload == null) {
            throw new RuntimeException("Failed to verify Google ID token.");
        }

        String email = payload.getEmail();
        String userId = payload.getSubject();
        String fullName = (String) payload.get("name");

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getProvider() != AuthProvider.GOOGLE) {
                throw new RuntimeException("User is already registered with different provider.");
            }

            user.setFullName(fullName);
            userRepository.save(user);
            String accessToken = jwtService.generateAccessToken(user);
            body.put("accessToken", accessToken);
            body.put("user", user);
            return body;
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setGoogleId(userId);
            newUser.setFullName(fullName);
            newUser.setProvider(AuthProvider.GOOGLE);
            newUser.setStatus(true);
            newUser.setRole(Role.valueOf(Role.BUYER.name()));

            String accessToken = jwtService.generateAccessToken(newUser);
            body.put("accessToken", accessToken);
            body.put("user", newUser);
            return body;
        }
    }

    public String forgotPassword(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new BusinessException("User does not exist", 404);
        }

        String otp = otpService.generateAndStoreOtp(email, OtpType.FORGOT_PASSWORD);
        mailService.sendOtpEmail(email, otp, OtpType.FORGOT_PASSWORD);

        return "OTP has been sent to your email.";
    }

    public String resetPassword(String email, String otpInput, String newPassword) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new BusinessException("User does not exist", 404);
        }

        otpService.verifyOtp(email, otpInput, OtpType.FORGOT_PASSWORD);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password has been reset successfully!";
    }
}




