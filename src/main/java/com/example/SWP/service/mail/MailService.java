package com.example.SWP.service.mail;

import com.example.SWP.enums.OtpType;
import com.example.SWP.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MailService {

    JavaMailSender mailSender;
    TemplateEngine templateEngine;

    private static final int OTP_EXPIRE_MINUTES = 15;

    /**
     * Gửi email chứa mã OTP cho người dùng.
     * Gồm các loại OTP (đăng ký, quên mật khẩu, ký hợp đồng, v.v.)
     */
    public void sendOtpEmail(String email, String otp, OtpType type) {
        // Lấy mục đích OTP theo loại
        String purposeVi = getVietnamesePurpose(type);
        String subject = "Mã xác thực (" + purposeVi + ") của bạn";

        // Chuẩn bị dữ liệu để render template HTML
        Context context = new Context();
        context.setVariable("otpCode", otp);
        context.setVariable("expireMinutes", OTP_EXPIRE_MINUTES);
        context.setVariable("otpPurpose", purposeVi);

        // Sinh nội dung email HTML từ template Thymeleaf
        String htmlContent = templateEngine.process("otp-email", context);

        // Gửi email HTML
        sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Gửi email HTML cho người nhận.
     */
    private void sendHtmlEmail(String email, String subject, String htmlContent) {
        // Kiểm tra đầu vào hợp lệ
        if (email == null || subject == null || htmlContent == null) {
            throw new BusinessException("Email, tiêu đề hoặc nội dung email không hợp lệ", 400);
        }

        try {
            // Tạo đối tượng email MIME
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            // Thiết lập thông tin người nhận và nội dung
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Gửi email
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new BusinessException("Gửi email đến " + email + " thất bại", 500);
        }
    }

    /**
     * Chuyển đổi loại OTP sang mục đích tiếng Việt.
     */
    private String getVietnamesePurpose(OtpType type) {
        return switch (type) {
            case REGISTER -> "Đăng ký tài khoản";
            case FORGOT_PASSWORD -> "Khôi phục mật khẩu";
            case CONTRACT_SIGN -> "Ký kết hợp đồng";
            default -> "Xác thực chung";
        };
    }
}
