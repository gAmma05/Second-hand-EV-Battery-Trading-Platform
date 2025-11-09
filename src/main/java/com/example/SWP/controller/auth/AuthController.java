package com.example.SWP.controller.auth;

import com.example.SWP.dto.request.auth.*;


import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.service.auth.AuthService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.validator.auth.CreateUserRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AuthController {

    AuthService authService;

    ValidateService validateService;

    @PostMapping("/google")
    private ResponseEntity googleLogin(@RequestBody GoogleLoginRequest glr) {
        List<String> tokens = authService.processGoogleToken(glr);
        String accessToken = tokens.get(0);
        String refreshToken = tokens.get(1);
        Map<String, Object> tokenBody = new HashMap<>();
        tokenBody.put("accessToken", accessToken);
        tokenBody.put("refreshToken", refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(tokenBody);
    }


    //Dang ky tai khoan va nhan ma OTP ve mail
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest request) {

        validateService.validatePassword(request.getPassword(), request.getConfirmPassword());

        String message = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<String>builder()
                        .success(true)
                        .message(message)
                        .build());
    }

    //Verify email thong qua ma otp
    @PostMapping("/verify-register")
    public ResponseEntity<ApiResponse<Void>> verifyRegister(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        authService.verifyRegister(email, otp);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Account verified successfully!")
                        .build()
        );
    }


    @Operation(
            summary = "Login user",
            description = "Login user with email and password"
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful"
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Wrong username or password"
    )

    @PostMapping("/basic-login")
    public ResponseEntity<Map<String, Object>> basicLogin(@Valid @RequestBody BasicLoginRequest dto) {
        Map<String, Object> response = authService.basicLogin(dto);
        return ResponseEntity.ok(response);
    }


    // Gửi OTP forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message(message)
                        .build()
        );
    }

    // Verify OTP và reset password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message(message)
                        .build()
        );
    }
}

