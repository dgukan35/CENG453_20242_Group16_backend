package com.group16.uno.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;  // Mock the JavaMailSender to avoid actual email sending

    @InjectMocks
    private EmailService emailService;  // Inject the mocked JavaMailSender into EmailService

    private String email;
    private String resetLink;
    private String token;

    @BeforeEach
    void setup() {
        email = "user@example.com";
        resetLink = "http://example.com/reset-password";
        token = "reset-token-12345";
    }

    @Test
    void sendResetEmail_shouldSendEmailWithCorrectDetails() {
        // Arrange
        SimpleMailMessage expectedMessage = new SimpleMailMessage();
        expectedMessage.setTo(email);
        expectedMessage.setSubject("Password Reset");
        expectedMessage.setText("To reset your password, click the link below. Since the frontend is not yet implemented, you can also copy the token and use it manually with the Set New Password endpoint.. Token: " + token + "\n" +
                "Resetlink:\n" + resetLink);

        // Act
        emailService.sendResetEmail(email, resetLink, token);

        // Assert
        verify(mailSender, times(1)).send(expectedMessage);  // Verify that the send method was called exactly once with the expected message
    }
}