package com.example.BTL_Mobile.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegalController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>BTL Mobile Backend</title>
                </head>
                <body style="font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; padding: 24px;">
                  <h2>BTL Mobile Backend</h2>
                  <p>Legal pages:</p>
                  <ul>
                    <li><a href="/privacy-policy">Privacy Policy</a></li>
                    <li><a href="/terms">Terms of Service</a></li>
                    <li><a href="/data-deletion">Data Deletion</a></li>
                  </ul>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/privacy-policy", produces = MediaType.TEXT_HTML_VALUE)
    public String privacyPolicy() {
        return """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>Privacy Policy</title>
                </head>
                <body style="font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; padding: 24px;">
                  <h2>Privacy Policy</h2>
                  <p>Ứng dụng học ngoại ngữ (BTL Mobile) thu thập thông tin tối thiểu để đăng nhập và sử dụng dịch vụ.</p>
                  <h3>Dữ liệu có thể thu thập</h3>
                  <ul>
                    <li>Email / tên hiển thị (nếu người dùng cung cấp qua Google/Facebook)</li>
                    <li>Thông tin tài khoản nội bộ (username)</li>
                  </ul>
                  <h3>Mục đích sử dụng</h3>
                  <ul>
                    <li>Xác thực người dùng</li>
                    <li>Cung cấp tính năng học tập và đồng bộ tiến trình</li>
                  </ul>
                  <h3>Liên hệ</h3>
                  <p>Email: <strong>nguyendung200444@gmail.com</strong></p>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/terms", produces = MediaType.TEXT_HTML_VALUE)
    public String terms() {
        return """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>Terms of Service</title>
                </head>
                <body style="font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; padding: 24px;">
                  <h2>Terms of Service</h2>
                  <p>Bằng việc sử dụng ứng dụng, bạn đồng ý không lạm dụng dịch vụ và tuân thủ quy định pháp luật.</p>
                  <p>Chúng tôi có thể cập nhật điều khoản theo thời gian.</p>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/data-deletion", produces = MediaType.TEXT_HTML_VALUE)
    public String dataDeletion() {
        return """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>Data Deletion</title>
                </head>
                <body style="font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; padding: 24px;">
                  <h2>Data Deletion Request</h2>
                  <p>Nếu bạn muốn xóa dữ liệu tài khoản, vui lòng gửi email với tiêu đề <strong>\"DELETE MY DATA\"</strong> kèm:</p>
                  <ul>
                    <li>Username hoặc email đã đăng ký</li>
                  </ul>
                  <p>Gửi tới: <strong>nguyendung200444@gmail.com</strong></p>
                </body>
                </html>
                """;
    }
}

