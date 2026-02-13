package com.secure.notes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.Collections;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name:Secure Notes}")
    private String fromName;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        try {
            logger.info("Attempting to send password reset email via Brevo to: {}", to);

            // Configure API client
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKey.setApiKey(brevoApiKey);

            // Create email API instance
            TransactionalEmailsApi api = new TransactionalEmailsApi();

            // Set sender
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(fromEmail);
            sender.setName(fromName);

            // Set recipient
            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(to);

            // Create email
            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(sender);
            email.setTo(Collections.singletonList(recipient));
            email.setSubject("Password Reset Request - Secure Notes");
            email.setHtmlContent(buildEmailContent(resetUrl));

            // Send email
            CreateSmtpEmail result = api.sendTransacEmail(email);

            logger.info("✅ Password reset email sent successfully to: {}. Message ID: {}", to, result.getMessageId());

        } catch (Exception e) {
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
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
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