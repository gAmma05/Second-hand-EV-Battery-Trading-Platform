package com.example.SWP.service;

import com.example.SWP.dto.request.BasicLoginRequest;
import com.example.SWP.dto.request.GoogleLoginRequest;
import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.User;
import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.OtpStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.jwt.JwtService;
import com.example.SWP.service.token.GoogleClientService;
import com.example.SWP.service.user.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    private GoogleClientService googleClientService;

    UserService userService;
    OtpService otpService;
    MailService mailService;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    private JwtService jwtService;

    @NonFinal
    @Value("${jwt.secret}")
    String jwtSecret;

    @NonFinal
    @Value("${jwt.issuer}")
    String jwtIssuer;

    public ApiResponse<String> register(RegisterRequest request) {
        String email = request.getEmail();

        User user = userService.findByEmail(email);
        if (user != null && user.isEnabled()) {
            return ApiResponse.<String>builder()
                    .success(false)
                    .message("Email is already in use!")
                    .build();
        }

        boolean isNewUser = (user == null);

        if (isNewUser) {
            userService.createInactiveUser(request);
        } else {
            log.info("User {} exists but not verified. Resending OTP.", email);
        }

        ApiResponse<String> response = otpService.generateAndStoreOtp(email);

        if (!response.isSuccess()) {
            return response;
        }


        mailService.sendOtpEmail(email, response.getData());

        return ApiResponse.<String>builder()
                .success(true)
                .message(isNewUser
                        ? "Registration successful! Please check your email for OTP."
                        : "Account already exists but not verified. A new OTP has been sent.")
                .build();
    }

    public ApiResponse<OtpStatus> verifyOtp(String email, String otpInput) {
        User user = userService.findByEmail(email);
        if (user == null || user.isEnabled()) {
            return ApiResponse.<OtpStatus>builder()
                    .success(false)
                    .message("Invalid request or user already verified.")
                    .data(OtpStatus.INVALID)
                    .build();
        }

        OtpStatus status = otpService.verifyOtp(email, otpInput);
        if (status == OtpStatus.SUCCESS) {
            userService.enableUser(user);
        }

        return ApiResponse.<OtpStatus>builder()
                .success(status == OtpStatus.SUCCESS)
                .message(status == OtpStatus.SUCCESS
                        ? "Account verified successfully!"
                        : "Invalid or expired OTP.")
                .data(status)
                .build();
    }


    public String basicLogin(BasicLoginRequest basicLoginRequest) {
        Optional<User> result = userRepository.findByEmail(basicLoginRequest.getEmail());

        if (result.isEmpty() || !result.get().isEnabled() || !passwordEncoder.matches(basicLoginRequest.getPassword(), result.get().getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password!");
        }

        User user = result.get();

        return jwtService.generateAccessToken(user);
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

        tokenList.add(jwtService.generateAccessToken(newUser));
        tokenList.add(jwtService.generateRefreshToken(newUser));

        return tokenList;
    }
}




