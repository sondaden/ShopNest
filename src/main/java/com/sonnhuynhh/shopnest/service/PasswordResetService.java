package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service xử lý quên mật khẩu và OTP
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Lưu OTP tạm thời (trong production nên dùng Redis)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Gửi OTP đến email
     */
    public boolean sendOtp(String email) {
        // Kiểm tra email có tồn tại không (không phân biệt hoa thường)
        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null) {
            logger.warn("Password reset requested for non-existent email: {}", email);
            return false;
        }
        
        // Sử dụng email đã lưu trong database (đúng định dạng)
        String actualEmail = user.getEmail();

        // Tạo OTP
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        // Lưu OTP (dùng email thực từ database)
        otpStorage.put(actualEmail.toLowerCase(), new OtpData(otp, expiryTime));
        
        // Log OTP để test (trong development)
        logger.info("===============================================");
        logger.info("OTP for {}: {}", actualEmail, otp);
        logger.info("Expires at: {}", expiryTime);
        logger.info("===============================================");

        // Gửi email
        String subject = "ShopNest - Mã xác nhận đặt lại mật khẩu";
        String content = buildOtpEmailContent(user.getFullName() != null ? user.getFullName() : user.getUsername(), otp);
        
        return emailService.sendHtmlEmail(actualEmail, subject, content);
    }

    /**
     * Xác thực OTP
     */
    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email.trim().toLowerCase());
        if (otpData == null) {
            return false;
        }

        // Kiểm tra hết hạn
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            otpStorage.remove(email);
            return false;
        }

        // Kiểm tra OTP đúng không
        return otpData.otp.equals(otp);
    }

    /**
     * Đặt lại mật khẩu
     */
    public boolean resetPassword(String email, String otp, String newPassword) {
        // Xác thực OTP trước
        if (!verifyOtp(email, otp)) {
            return false;
        }

        // Tìm user và cập nhật mật khẩu (không phân biệt hoa thường)
        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa OTP sau khi sử dụng
        otpStorage.remove(email.trim().toLowerCase());

        return true;
    }

    /**
     * Tạo OTP ngẫu nhiên
     */
    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Tạo nội dung email HTML
     */
    private String buildOtpEmailContent(String userName, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #0d6efd, #0dcaf0); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .otp-box { background: white; border: 2px dashed #0d6efd; padding: 20px; text-align: center; margin: 20px 0; border-radius: 10px; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #0d6efd; letter-spacing: 8px; }
                    .warning { color: #dc3545; font-size: 14px; margin-top: 20px; }
                    .footer { text-align: center; color: #6c757d; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛒 ShopNest</h1>
                        <p>Đặt lại mật khẩu</p>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản ShopNest của mình.</p>
                        <p>Đây là mã xác nhận của bạn:</p>
                        <div class="otp-box">
                            <div class="otp-code">%s</div>
                        </div>
                        <p>Mã xác nhận này sẽ hết hạn sau <strong>%d phút</strong>.</p>
                        <p class="warning">⚠️ Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 ShopNest. Mọi quyền được bảo lưu.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, otp, OTP_EXPIRY_MINUTES);
    }

    /**
     * Inner class lưu OTP data
     */
    private record OtpData(String otp, LocalDateTime expiryTime) {}
}
