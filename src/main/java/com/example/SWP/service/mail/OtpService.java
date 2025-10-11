package com.example.SWP.service.mail;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.enums.OtpStatus;
import com.example.SWP.exception.BusinessException;
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

    public String generateAndStoreOtp(String email) {
        // Kiểm tra lock
        String lockKey = getLockKey(email);
        String lockedUntilStr = redisTemplate.opsForValue().get(lockKey);
        if (lockedUntilStr != null && LocalDateTime.parse(lockedUntilStr).isAfter(LocalDateTime.now())) {
            throw new BusinessException("You have been locked out due to multiple failed OTP attempts. Please try again later.", 403);
        }

        // Kiểm tra cooldown
        String otpRequestKey = getOtpRequestKey(email);
        String lastRequestTimeStr = redisTemplate.opsForValue().get(otpRequestKey);
        if (lastRequestTimeStr != null && LocalDateTime.parse(lastRequestTimeStr).plusMinutes(1).isAfter(LocalDateTime.now())) {
            throw new BusinessException("You can only request a new OTP once every minute. Please try again later.", 400);
        }

        // Tạo OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        redisTemplate.delete(getOtpKey(email));
        redisTemplate.delete(getAttemptKey(email));
        redisTemplate.opsForValue().set(getOtpKey(email), otp, OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(getAttemptKey(email), "0", OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(otpRequestKey, LocalDateTime.now().toString(), 1, TimeUnit.MINUTES);

        return otp;
    }


    public void verifyOtp(String email, String otpInput) {
        String otpKey = getOtpKey(email);
        String attemptKey = getAttemptKey(email);
        String lockKey = getLockKey(email);

        // Kiểm tra lock
        String lockedUntilStr = redisTemplate.opsForValue().get(lockKey);
        if (lockedUntilStr != null) {
            LocalDateTime lockedUntil = LocalDateTime.parse(lockedUntilStr);
            if (lockedUntil.isAfter(LocalDateTime.now())) {
                throw new BusinessException("Too many failed OTP attempts. Account temporarily locked.", 403);
            } else {
                redisTemplate.delete(lockKey);
            }
        }

        // Kiểm tra OTP
        String cachedOtp = redisTemplate.opsForValue().get(otpKey);
        if (cachedOtp != null && cachedOtp.equals(otpInput)) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            redisTemplate.delete(lockKey);
            return; // thành công
        }

        // Tăng số lần thử
        String attemptVal = redisTemplate.opsForValue().get(attemptKey);
        if (attemptVal == null) {
            attemptVal = "0";
        }
        int attempts = Integer.parseInt(attemptVal) + 1;

        if (attempts >= MAX_OTP_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            redisTemplate.opsForValue().set(lockKey, lockUntil.toString(), LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey);
            throw new BusinessException("Too many failed OTP attempts. Account temporarily locked.", 403);
        }

        redisTemplate.opsForValue().set(attemptKey, String.valueOf(attempts), OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        throw new BusinessException("Invalid or expired OTP.", 400);
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

