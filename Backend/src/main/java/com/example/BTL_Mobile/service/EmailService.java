package com.example.BTL_Mobile.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.from-name:ELearn}")
    private String fromName;

    public void sendOtp(String to, String subject, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildOtpBody(otp), false);

            if (fromEmail != null && !fromEmail.isBlank()) {
                // Display name shown to receiver (e.g. "ELearn <your@gmail.com>")
                helper.setFrom(fromEmail, fromName);
            }

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // Log nhưng không để lộ nội dung nhạy cảm
            log.error("Failed to send OTP email to {}", to, e);
        }
    }

    private String buildOtpBody(String otp) {
        return "Mã xác thực của bạn là: " + otp + "\n\n"
                + "Mã có hiệu lực trong 10 phút.\n"
                + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.";
    }
}

