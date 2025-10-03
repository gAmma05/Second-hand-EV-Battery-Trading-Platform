package com.example.SWP.controller.auth;

import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.enums.OtpStatus;
import com.example.SWP.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String result = userService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        OtpStatus status = userService.verifyOtp(email, otp);
        if (status == OtpStatus.SUCCESS) {
            return ResponseEntity.ok(status.name());
        } else {
            return ResponseEntity.badRequest().body(status.name());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        String result = userService.resendOtp(email);
        return ResponseEntity.ok(result);
    }
}

