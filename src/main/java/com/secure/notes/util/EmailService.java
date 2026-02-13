//package com.secure.notes.util;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    public void sendPasswordResetEmail(String to, String resetUrl) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Password Reset Request");
//        message.setText(
//                "Hello,\n\n" +
//                        "We received a request to reset the password for your Secure Notes account.\n\n" +
//                        "To reset your password, please click the link below:\n" +
//                        resetUrl + "\n\n" +
//                        "This password reset link is valid for 15 minutes only. After that, you will need to request a new link.\n\n" +
//                        "If you did not request a password reset, please ignore this email—your account will remain secure.\n\n" +
//                        "Thank you for using Secure Notes.\n" +
//                        "The Secure Notes Team"
//        );
//        mailSender.send(message);
//    }
//
//}


//package com.secure.notes.util;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.MailException;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    @Retryable(
//            value = {MailException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 2000)
//    )
//    public void sendPasswordResetEmail(String to, String resetUrl) {
//        try {
//            logger.info("Attempting to send password reset email to: {}", to);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject("Password Reset Request - Secure Notes");
//
//            String emailContent = buildEmailContent(resetUrl);
//            helper.setText(emailContent, true); // true = HTML content
//
//            mailSender.send(message);
//            logger.info("Password reset email sent successfully to: {}", to);
//
//        } catch (MessagingException e) {
//            logger.error("Failed to create email message for: {}", to, e);
//            throw new RuntimeException("Failed to send password reset email", e);
//        } catch (MailException e) {
//            logger.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
//            throw new RuntimeException("Failed to send password reset email. Please try again later.", e);
//        }
//    }
//
//    private String buildEmailContent(String resetUrl) {
//        return """
//                <!DOCTYPE html>
//                <html>
//                <head>
//                    <style>
//                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
//                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
//                        .button {
//                            display: inline-block;
//                            padding: 12px 24px;
//                            background-color: #007bff;
//                            color: white;
//                            text-decoration: none;
//                            border-radius: 5px;
//                            margin: 20px 0;
//                        }
//                        .footer { margin-top: 30px; font-size: 12px; color: #666; }
//                    </style>
//                </head>
//                <body>
//                    <div class="container">
//                        <h2>Password Reset Request</h2>
//                        <p>Hello,</p>
//                        <p>We received a request to reset the password for your Secure Notes account.</p>
//                        <p>To reset your password, please click the button below:</p>
//                        <a href="%s" class="button">Reset Password</a>
//                        <p>Or copy and paste this link into your browser:</p>
//                        <p style="word-break: break-all; color: #007bff;">%s</p>
//                        <p><strong>This password reset link is valid for 15 minutes only.</strong> After that, you will need to request a new link.</p>
//                        <p>If you did not request a password reset, please ignore this email—your account will remain secure.</p>
//                        <div class="footer">
//                            <p>Thank you for using Secure Notes.</p>
//                            <p>The Secure Notes Team</p>
//                        </div>
//                    </div>
//                </body>
//                </html>
//                """.formatted(resetUrl, resetUrl);
//    }
//}

package com.secure.notes.util;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:Secure Notes}")
    private String fromName;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        try {
            logger.info("Attempting to send password reset email via SendGrid to: {}", to);

            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            String subject = "Password Reset Request - Secure Notes";

            Content content = new Content("text/html", buildEmailContent(resetUrl));
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Password reset email sent successfully to: {}. Status: {}", to, response.getStatusCode());
            } else {
                logger.error("❌ Failed to send email. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send password reset email. Status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            logger.error("❌ Failed to send email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email. Please try again later.", e);
        }
    }

    private String buildEmailContent(String resetUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
                            line-height: 1.6; 
                            color: #333;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 0;
                        }
                        .email-container {
                            max-width: 600px;
                            margin: 20px auto;
                            background-color: #ffffff;
                            border-radius: 8px;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        }
                        .header { 
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white; 
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content { 
                            padding: 40px 30px;
                        }
                        .content p {
                            margin: 0 0 15px 0;
                            font-size: 16px;
                        }
                        .button-container {
                            text-align: center;
                            margin: 30px 0;
                        }
                        .button { 
                            display: inline-block; 
                            padding: 14px 32px;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white !important;
                            text-decoration: none; 
                            border-radius: 6px;
                            font-weight: 600;
                            font-size: 16px;
                            box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
                        }
                        .link-box {
                            background-color: #f8f9fa;
                            padding: 15px;
                            border-radius: 6px;
                            margin: 20px 0;
                            word-break: break-all;
                        }
                        .link-box a {
                            color: #667eea;
                            text-decoration: none;
                            font-size: 13px;
                        }
                        .warning-box {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .warning-box p {
                            margin: 0;
                            color: #856404;
                            font-size: 14px;
                        }
                        .footer { 
                            background-color: #f8f9fa;
                            padding: 25px 30px;
                            text-align: center;
                            border-top: 1px solid #e9ecef;
                        }
                        .footer p {
                            margin: 5px 0;
                            font-size: 13px;
                            color: #666;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>🔒 Secure Notes</h1>
                        </div>
                        
                        <div class="content">
                            <h2 style="color: #333; margin-top: 0;">Password Reset Request</h2>
                            <p>Hello,</p>
                            <p>We received a request to reset the password for your Secure Notes account.</p>
                            
                            <div class="button-container">
                                <a href="%s" class="button">Reset Your Password</a>
                            </div>
                            
                            <div class="link-box">
                                <p style="margin-bottom: 8px; font-weight: 600; color: #333;">Or copy and paste this link:</p>
                                <a href="%s">%s</a>
                            </div>
                            
                            <div class="warning-box">
                                <p><strong>⏰ Time Sensitive:</strong> This link expires in <strong>15 minutes</strong>. After that, you'll need to request a new one.</p>
                            </div>
                            
                            <p style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e9ecef;">
                                <strong>🛡️ Security Note:</strong><br>
                                If you didn't request this password reset, please ignore this email. Your account remains secure and no changes will be made.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>Secure Notes Team</strong></p>
                            <p>Keeping your notes safe and secure</p>
                            <p style="margin-top: 15px; font-size: 11px;">This is an automated message. Please don't reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetUrl, resetUrl, resetUrl);
    }
}