package com.secure.notes.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset the password for your Secure Notes account.\n\n" +
                        "To reset your password, please click the link below:\n" +
                        resetUrl + "\n\n" +
                        "This password reset link is valid for 15 minutes only. After that, you will need to request a new link.\n\n" +
                        "If you did not request a password reset, please ignore this email—your account will remain secure.\n\n" +
                        "Thank you for using Secure Notes.\n" +
                        "The Secure Notes Team"
        );
        mailSender.send(message);
    }

}
