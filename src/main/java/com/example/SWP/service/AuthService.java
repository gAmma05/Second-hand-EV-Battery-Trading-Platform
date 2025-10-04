package com.example.SWP.service;

import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OtpStatus;
import com.nimbusds.jose.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserService userService;
    OtpService otpService;
    MailService mailService;

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
}




