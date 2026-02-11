package com.example.BTL_Mobile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtp(String to, String subject, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(buildOtpBody(otp));
            mailSender.send(message);
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

