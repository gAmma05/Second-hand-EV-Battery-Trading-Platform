package com.example.SWP.controller.auth;

import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        String siteURL = servletRequest.getRequestURL().toString().replace(servletRequest.getServletPath(), "");
        String result = userService.register(request, siteURL);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("code") String code) {
        boolean verified = userService.verify(code);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully! You can now login.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification code.");
        }
    }
}
