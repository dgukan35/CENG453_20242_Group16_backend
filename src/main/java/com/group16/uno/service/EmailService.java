package com.group16.uno.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String to, String resetLink, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset");
        message.setText("To reset your password, click the link below. Since the frontend is not yet implemented, you can also copy the token and use it manually with the Set New Password endpoint.. Token: " + token + "\n" +
                "Resetlink:\n"
                + resetLink);
        mailSender.send(message);
    }
}
