package com.example.SWP.service.mail;

import com.example.SWP.enums.OtpType;
import com.example.SWP.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MailService {

    JavaMailSender mailSender;

    private static final int OTP_EXPIRE_MINUTES = 15;

    public void sendOtpEmail(String email, String otp, OtpType type) {
        String subject = "Your OTP for " + type;

        String content = "Your OTP is: " + otp +
                "\nIt will expire in " + OTP_EXPIRE_MINUTES + " minutes.";

        sendEmail(email, subject, content);
    }

    public void sendEmail(String email, String subject, String content) {
        if (email == null || subject == null || content == null) {
            throw new BusinessException("Email, subject or content is null", 400);
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(content);
            mailSender.send(mailMessage);
        } catch (Exception e) {
            throw new BusinessException("Failed to send email to " + email, 500);
        }
    }

}
