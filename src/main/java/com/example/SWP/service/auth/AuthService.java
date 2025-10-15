package com.example.SWP.service.auth;

import com.example.SWP.dto.request.auth.BasicLoginRequest;
import com.example.SWP.dto.request.auth.GoogleLoginRequest;
import com.example.SWP.dto.request.auth.CreateUserRequest;

import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.entity.User;
import com.example.SWP.enums.AuthProvider;
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

        User user = userService.findByEmail(email);
        if (user != null && user.isEnabled()) {
            throw new BusinessException("Email is already in use!", 400);
        }

        boolean isNewUser = (user == null);

        if (isNewUser) {
            userService.createInactiveUser(request);
        } else {
            log.info("User {} exists but not verified. Resending OTP.", email);
        }

        String otp = otpService.generateAndStoreOtp(email);

        mailService.sendOtpEmail(email, otp);

        return isNewUser
                ? "Registration successful! Please check your email for OTP."
                : "Account already exists but not verified. A new OTP has been sent.";
    }


    public void verifyRegister(String email, String otpInput) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new BusinessException("User does not exist", 404);
        }
        if (user.isEnabled()) {
            throw new BusinessException("User is already verified", 400);
        }

        otpService.verifyOtp(email, otpInput);
        userService.enableUser(user);
        createNotification(user, "Welcome to Second-hand EV Battery Trading Platform!", "You have successfully registered to our platform. " +
                "Please fill in your profile information to complete your profile before purchasing. " +
                "Thank you!");
    }


    public Map<String, Object> basicLogin(BasicLoginRequest basicLoginRequest) {
        Optional<User> result = userRepository.findByEmail(basicLoginRequest.getEmail());

        if (result.isEmpty() || !result.get().isEnabled() ||
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
    public List<String> processGoogleToken(GoogleLoginRequest googleLoginRequest) {
        List<String> tokenList = new ArrayList<>();

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
            tokenList.add(jwtService.generateAccessToken(user));
            tokenList.add(jwtService.generateRefreshToken(user));

            return tokenList;
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setGoogleId(userId);
        newUser.setFullName(fullName);
        newUser.setProvider(AuthProvider.GOOGLE);
        newUser.setRole(Role.valueOf(Role.BUYER.name()));
        newUser.setEnabled(true);

        userRepository.save(newUser);

        createNotification(newUser, "Welcome to Second-hand EV Battery Trading Platform!", "You have successfully registered to our platform. " +
                "Please fill in your profile information to complete your profile before purchasing. " +
                "Thank you!");

        tokenList.add(jwtService.generateAccessToken(newUser));
        tokenList.add(jwtService.generateRefreshToken(newUser));

        return tokenList;
    }

    private void createNotification(User user, String title, String content) {
        notificationService.sendNotificationToOneUser(user.getEmail(), title, content);
    }

    public String forgotPassword(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new BusinessException("User does not exist", 404);
        }

        String otp = otpService.generateAndStoreOtp(email);
        mailService.sendOtpEmail(email, otp);

        return "OTP has been sent to your email.";
    }

    public String resetPassword(String email, String otpInput, String newPassword) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new BusinessException("User does not exist", 404);
        }

        otpService.verifyOtp(email, otpInput);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        createNotification(user, "Reset password", "You have successfully changed the password");

        return "Password has been reset successfully!";
    }
}




