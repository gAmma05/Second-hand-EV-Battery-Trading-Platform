package com.example.SWP.validator.auth;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class GoogleAuthValidator {
    public void validateGoogleClaims(Map<String, Object> claims) {
        validateEmail(claims);
        validateName(claims);
    }

    private void validateEmail(Map<String, Object> claims) {
        String email = (String) claims.get("email");
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
    }

    private void validateName(Map<String, Object> claims) {
        String name = (String) claims.get("name");
        if (name == null || name.isEmpty()) {
            log.warn("Name is not present in Google claims.");
        }
    }

}
