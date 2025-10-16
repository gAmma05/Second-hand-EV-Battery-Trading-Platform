package com.example.SWP.service.mail;

import com.example.SWP.dto.request.auth.CreateUserRequest;
import com.example.SWP.enums.OtpType;
import com.example.SWP.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OtpService {

    final RedisTemplate<String, String> redisTemplate;
    final ObjectMapper objectMapper;

    @NonFinal
    @org.springframework.beans.factory.annotation.Value("${otp.expire-minutes}")
    private int otpExpireMinutes;

    @Value("${otp.max-attempts}")
    private int maxOtpAttempts;

    @Value("${otp.lock-duration-minutes}")
    private int lockDurationMinutes;

    @Value("${otp.cooldown-minutes}")
    private int cooldownMinutes;

    public String generateAndStoreOtp(String email, OtpType type) {
        String lockKey = getLockKey(email, type);
        String lockedUntilStr = redisTemplate.opsForValue().get(lockKey);
        if (lockedUntilStr != null && LocalDateTime.parse(lockedUntilStr).isAfter(LocalDateTime.now())) {
            throw new BusinessException("You have been locked out due to multiple failed OTP attempts. Please try again later.", 403);
        }

        String otpRequestKey = getOtpRequestKey(email, type);
        String lastRequestTimeStr = redisTemplate.opsForValue().get(otpRequestKey);
        if (lastRequestTimeStr != null && LocalDateTime.parse(lastRequestTimeStr)
                .plusMinutes(cooldownMinutes).isAfter(LocalDateTime.now())) {
            throw new BusinessException("You can only request a new OTP once every minute. Please try again later.", 400);
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        redisTemplate.delete(getOtpKey(email, type));
        redisTemplate.delete(getAttemptKey(email, type));
        redisTemplate.opsForValue().set(getOtpKey(email, type), otp, otpExpireMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(getAttemptKey(email, type), "0", otpExpireMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(otpRequestKey, LocalDateTime.now().toString(), 1, TimeUnit.MINUTES);

        return otp;
    }

    public void verifyOtp(String email, String otpInput, OtpType type) {
        String otpKey = getOtpKey(email, type);
        String attemptKey = getAttemptKey(email, type);
        String lockKey = getLockKey(email, type);

        String lockedUntilStr = redisTemplate.opsForValue().get(lockKey);
        if (lockedUntilStr != null) {
            LocalDateTime lockedUntil = LocalDateTime.parse(lockedUntilStr);
            if (lockedUntil.isAfter(LocalDateTime.now())) {
                throw new BusinessException("Too many failed OTP attempts. Email temporarily locked.", 403);
            } else {
                redisTemplate.delete(lockKey);
            }
        }

        String cachedOtp = redisTemplate.opsForValue().get(otpKey);
        if (cachedOtp != null && cachedOtp.equals(otpInput)) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            redisTemplate.delete(lockKey);
            return;
        }

        String attemptVal = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptVal == null ? 1 : Integer.parseInt(attemptVal) + 1;

        if (attempts >= maxOtpAttempts) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
            redisTemplate.opsForValue().set(lockKey, lockUntil.toString(), lockDurationMinutes, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey);
            throw new BusinessException("Too many failed OTP attempts. Account temporarily locked.", 403);
        }

        redisTemplate.opsForValue().set(attemptKey, String.valueOf(attempts), otpExpireMinutes, TimeUnit.MINUTES);
        throw new BusinessException("Invalid or expired OTP.", 400);
    }

    public void storePendingRegistration(String email, CreateUserRequest request) {
        try {
            String key = getRegistrationKey(email);

            // Kiểm tra xem email đó đã có đăng ký đang chờ chưa
            Boolean exists = redisTemplate.hasKey(key);
            if (exists) {
                throw new BusinessException("This email already has a pending registration. Please verify your OTP first.", 400);
            }

            // Lưu thông tin đăng ký (nếu chưa tồn tại)
            String json = objectMapper.writeValueAsString(request);
            redisTemplate.opsForValue().setIfAbsent(
                    key,
                    json,
                    otpExpireMinutes,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to store registration data", 500);
        }
    }


    public CreateUserRequest getPendingRegistration(String email) {
        try {
            String json = redisTemplate.opsForValue().get(getRegistrationKey(email));
            return json != null ? objectMapper.readValue(json, CreateUserRequest.class) : null;
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to read registration data", 500);
        }
    }

    public void deletePendingRegistration(String email) {
        redisTemplate.delete(getRegistrationKey(email));
    }


    private String getOtpKey(String email, OtpType type) {
        return "OTP:" + type.name() + ":" + email;
    }

    private String getAttemptKey(String email, OtpType type) {
        return "OTP_ATTEMPT:" + type.name() + ":" + email;
    }

    private String getLockKey(String email, OtpType type) {
        return "OTP_LOCK:" + type.name() + ":" + email;
    }

    private String getOtpRequestKey(String email, OtpType type) {
        return "OTP_REQUEST:" + type.name() + ":" + email;
    }

    private String getRegistrationKey(String email) {
        return "PENDING_REGISTRATION:" + email;
    }
}

