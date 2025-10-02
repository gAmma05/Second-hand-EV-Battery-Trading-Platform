package com.example.SWP.service;

import com.example.SWP.dto.request.RegisterRequest;
import com.example.SWP.entity.User;
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

    // Register user và gửi OTP
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email is already in use!";
        }

        // Tạo OTP 6 chữ số
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER.name())
                .enabled(false)
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5)) // OTP hiệu lực 5 phút
                .build();

        userRepository.save(user);

        sendOtpEmail(user, otp);

        return "Registration successful! Please check your email for OTP.";
    }

    // Gửi OTP qua email
    private void sendOtpEmail(User user, String otp) {
        String subject = "Your OTP for registration";
        String message = "Dear user,\nYour OTP is: " + otp + "\nIt will expire in 5 minutes.";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    // Verify OTP
    public boolean verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isEnabled()) return false;

        if (!otp.equals(user.getOtpCode())) return false;
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) return false;

        user.setEnabled(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return true;
    }

    // Resend OTP mới, OTP cũ tự động invalid
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
