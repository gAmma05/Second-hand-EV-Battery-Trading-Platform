package com.example.SWP.service;

import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OtpStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JavaMailSender mailSender;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email is already in use!";
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER.name())
                .fullName(request.getFullName())
                .enabled(false)
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5))
                .build();

        userRepository.save(user);

        sendOtpEmail(user, otp);

        return "Registration successful! Please check your email for OTP.";
    }

    private void sendOtpEmail(User user, String otp) {
        String subject = "Your OTP for registration";
        String message = "Dear user,\nYour OTP is: " + otp + "\nIt will expire in 5 minutes.";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    public OtpStatus verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return OtpStatus.INVALID;
        }

        if (user.getOtpLockedUntil() != null && user.getOtpLockedUntil().isAfter(LocalDateTime.now())) {
            return OtpStatus.LOCKED;
        }

        if (user.getOtpCode() != null && user.getOtpExpiry().isAfter(LocalDateTime.now()) && user.getOtpCode().equals(otp)) {
            user.setEnabled(true);
            user.setOtpCode(null);
            user.setOtpAttempts(0);
            user.setOtpLockedUntil(null);
            userRepository.save(user);
            return OtpStatus.SUCCESS;
        } else {
            int attempts = user.getOtpAttempts() + 1;
            user.setOtpAttempts(attempts);

            if (attempts >= 5) {
                user.setOtpLockedUntil(LocalDateTime.now().plusMinutes(30));
                user.setOtpAttempts(0);
                userRepository.save(user);
                return OtpStatus.LOCKED;
            }

            userRepository.save(user);
            return OtpStatus.INVALID;
        }
    }


    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "Email not found.";
        if (user.isEnabled()) return "Account already verified.";

        String newOtp = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setOtpCode(newOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        sendOtpEmail(user, newOtp);

        return "A new OTP has been sent.";
    }
}
