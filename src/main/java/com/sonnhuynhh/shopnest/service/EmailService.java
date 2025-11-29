package com.sonnhuynhh.shopnest.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service gửi email
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Kiểm tra email đã được cấu hình chưa
     */
    private boolean isEmailConfigured() {
        logger.info("Checking email config - fromEmail value: '{}'", fromEmail);
        boolean configured = fromEmail != null 
            && !fromEmail.isEmpty() 
            && !fromEmail.equals("noreply@shopnest.com") 
            && !fromEmail.contains("your-email")
            && fromEmail.contains("@");
        logger.info("Email configured: {}", configured);
        return configured;
    }

    /**
     * Gửi email HTML
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        // Kiểm tra email đã được cấu hình chưa
        if (!isEmailConfigured()) {
            logger.warn("===============================================");
            logger.warn("EMAIL SERVICE IS NOT CONFIGURED!");
            logger.warn("Email would be sent to: {}", to);
            logger.warn("Subject: {}", subject);
            logger.warn("Please configure spring.mail.* properties");
            logger.warn("===============================================");
            return true; // Return true để flow tiếp tục (cho mục đích test)
        }
        
        try {
            logger.info("Attempting to send email to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gửi email text đơn giản
     */
    public boolean sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            return true;
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Gửi email bất đồng bộ
     */
    @Async
    public void sendHtmlEmailAsync(String to, String subject, String htmlContent) {
        sendHtmlEmail(to, subject, htmlContent);
    }
}
