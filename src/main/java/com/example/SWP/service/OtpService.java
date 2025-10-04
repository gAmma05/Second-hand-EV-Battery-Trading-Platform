package com.example.SWP.service;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.enums.OtpStatus;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class OtpService {

    RedisTemplate<String, String> redisTemplate;

    private static final int OTP_EXPIRE_MINUTES = 15;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    public ApiResponse<String> generateAndStoreOtp(String email) {
        String lockKey = getLockKey(email);
        String lockedUntilStr = redisTemplate.opsForValue().get(lockKey);

        if (lockedUntilStr != null && LocalDateTime.parse(lockedUntilStr).isAfter(LocalDateTime.now())) {
            return ApiResponse.<String>builder()
                    .success(false)
                    .message("You have been locked out due to multiple failed OTP attempts. Please try again later.")
                    .build();
        }

        String otpRequestKey = getOtpRequestKey(email);
        String lastRequestTimeStr = redisTemplate.opsForValue().get(otpRequestKey);

        if (lastRequestTimeStr != null) {
            LocalDateTime lastRequestTime = LocalDateTime.parse(lastRequestTimeStr);
            if (lastRequestTime.plusMinutes(1).isAfter(LocalDateTime.now())) {
                return ApiResponse.<String>builder()
                        .success(false)
                        .message("You can only request a new OTP once every minute. Please try again later.")
                        .build();
            }
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        redisTemplate.delete(getOtpKey(email));
        redisTemplate.delete(getAttemptKey(email));

        redisTemplate.opsForValue().set(getOtpKey(email), otp, OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(getAttemptKey(email), "0", OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(otpRequestKey, LocalDateTime.now().toString(), 1, TimeUnit.MINUTES);

        return ApiResponse.<String>builder()
                .success(true)
                .message("OTP generated and sent successfully.")
                .data(otp)
                .build();
    }

    public OtpStatus verifyOtp(String email, String otpInput) {
        String otpKey = getOtpKey(email);
        String attemptKey = getAttemptKey(email);
        String lockKey = getLockKey(email);

        String cachedOtp = redisTemplate.opsForValue().get(otpKey);
        if (cachedOtp != null && cachedOtp.equals(otpInput)) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            redisTemplate.delete(lockKey);
            return OtpStatus.SUCCESS;
        }

        String attemptVal = redisTemplate.opsForValue().get(attemptKey);
        if (attemptVal == null) {
            attemptVal = "0";
        }
        int attempts = Integer.parseInt(attemptVal) + 1;

        if (attempts >= MAX_OTP_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            redisTemplate.opsForValue().set(lockKey, lockUntil.toString(), LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey);
            return OtpStatus.LOCKED;
        }

        redisTemplate.opsForValue().set(attemptKey, String.valueOf(attempts), OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return OtpStatus.INVALID;
    }

    private String getOtpKey(String email) {
        return "OTP:" + email;
    }

    private String getAttemptKey(String email) {
        return "OTP_ATTEMPT:" + email;
    }

    private String getLockKey(String email) {
        return "OTP_LOCK:" + email;
    }

    private String getOtpRequestKey(String email) {
        return "OTP_REQUEST_" + email;
    }
}

