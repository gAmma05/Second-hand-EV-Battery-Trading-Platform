package com.example.SWP.controller.auth;

import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.enums.OtpStatus;
import com.example.SWP.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OtpStatus>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        return ResponseEntity.ok(authService.verifyOtp(email, otp));
    }


//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
//        String token = authService.login(request);
//        return ResponseEntity.ok(new LoginResponse(token));
//    }
}

