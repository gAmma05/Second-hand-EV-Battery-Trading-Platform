package com.example.SWP.controller.auth;

import com.example.SWP.dto.request.auth.BasicLoginRequest;
import com.example.SWP.dto.request.auth.GoogleLoginRequest;
import com.example.SWP.dto.request.auth.CreateUserRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.enums.OtpStatus;
import com.example.SWP.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;

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


    @Operation(
            summary = "Register a user",
            description = "Register a user with email, password and full name"
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully"
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Email is already in use or user already verified"
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody CreateUserRequest request) {
        ApiResponse<String> response = authService.register(request);
        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "Verify account",
            description = "Verify account with email and OTP"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account verified successfully"
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid OTP or user already verified"
    )
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OtpStatus>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        ApiResponse<OtpStatus> response = authService.verifyOtp(email, otp);
        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
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
    public ResponseEntity<Map<String, Object>> basicLogin(@RequestBody BasicLoginRequest dto) {
        String accessToken = authService.basicLogin(dto);

        Map<String, Object> body = new HashMap<>();
        body.put("accessToken", accessToken);

        return ResponseEntity.ok(body);
    }

}

