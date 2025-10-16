package com.example.SWP.service.mail;

import com.example.SWP.enums.OtpType;
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
        String subject = (type == OtpType.REGISTER)
                ? "Your OTP for registration"
                : "Your OTP for password reset";

        String text = "Dear user,\n\nYour OTP is: " + otp +
                "\nIt will expire in " + OTP_EXPIRE_MINUTES + " minutes.\n\nThank you!";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        mailSender.send(mailMessage);
    }

}
