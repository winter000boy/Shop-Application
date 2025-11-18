package com.shopmanagement.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Service for sending email notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@shopmanagement.com}")
    private String fromEmail;
    
    /**
     * Send a simple email
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    
    /**
     * Send order status update email
     */
    public void sendOrderStatusEmail(String to, String customerName, String deviceModel, 
                                     String status, String shopName) {
        String subject = "Order Status Update - " + shopName;
        String body = String.format(
            "Dear %s,\n\n" +
            "Your repair order for %s has been updated.\n\n" +
            "Current Status: %s\n\n" +
            "Thank you for choosing %s.\n\n" +
            "Best regards,\n%s",
            customerName, deviceModel, status, shopName, shopName
        );
        
        sendEmail(to, subject, body);
    }
    
    /**
     * Send order completion email
     */
    public void sendOrderCompletionEmail(String to, String customerName, String deviceModel, 
                                        String shopName, String shopPhone) {
        String subject = "Your Device is Ready - " + shopName;
        String body = String.format(
            "Dear %s,\n\n" +
            "Great news! Your %s repair is complete and ready for pickup.\n\n" +
            "Please visit our shop at your convenience to collect your device.\n\n" +
            "Contact us: %s\n\n" +
            "Thank you for choosing %s.\n\n" +
            "Best regards,\n%s",
            customerName, deviceModel, shopPhone, shopName, shopName
        );
        
        sendEmail(to, subject, body);
    }
    
    /**
     * Send email with PDF attachment
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @param attachmentPath Path to the attachment file (URL or file path)
     */
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            
            // Extract file path from URL if needed
            String filePath = attachmentPath;
            if (attachmentPath.startsWith("http://") || attachmentPath.startsWith("https://")) {
                // For local storage, convert URL to file path
                // This is a simplified approach - in production, you might need to download the file
                filePath = attachmentPath.replace("http://localhost:8080/uploads/", "uploads/");
            }
            
            File file = new File(filePath);
            if (file.exists()) {
                FileSystemResource fileResource = new FileSystemResource(file);
                helper.addAttachment(file.getName(), fileResource);
            } else {
                log.warn("Attachment file not found: {}", filePath);
            }
            
            mailSender.send(message);
            log.info("Email with attachment sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email with attachment to: {}", to, e);
            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage());
        }
    }
}
