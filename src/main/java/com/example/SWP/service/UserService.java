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

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)

public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JavaMailSender mailSender;

    public String register(RegisterRequest request, String siteURL) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email is already in use!";
        }

        String verificationCode = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER.name())
                .enabled(false)
                .verificationCode(verificationCode)
                .build();

        userRepository.save(user);

        sendVerificationEmail(user, siteURL);

        return "Registration successful! Please check your email to verify your account.";
    }

    private void sendVerificationEmail(User user, String siteURL) {
        String subject = "Please verify your registration";
        String verifyURL = siteURL + "/api/auth/verify?code=" + user.getVerificationCode();
        String message = "Dear user,\nPlease click the link below to verify your email:\n" + verifyURL;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    public boolean verify(String code) {
        User user = userRepository.findByVerificationCode(code).orElse(null);
        if (user == null || user.isEnabled()) {
            return false;
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        userRepository.save(user);
        return true;
    }
}