package com.example.SWP.controller.auth;

import com.example.SWP.dto.request.BasicLoginRequest;
import com.example.SWP.dto.request.GoogleLoginRequest;
import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.enums.OtpStatus;
import com.example.SWP.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    private ResponseEntity googleLogin(@RequestBody GoogleLoginRequest glr) {
        String accessToken = authService.processGoogleToken(glr);
        return new ResponseEntity(new HashMap<String, Object>() {{
            put("accessToken", accessToken);
        }}, HttpStatus.CREATED);
    }

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

    @PostMapping("/basic-login")
    public ResponseEntity<Map<String, Object>> basicLogin(@RequestBody BasicLoginRequest dto) {
        String accessToken = authService.basicLogin(dto);

        Map<String, Object> body = new HashMap<>();
        body.put("accessToken", accessToken);

        return ResponseEntity.ok(body);
    }

}

