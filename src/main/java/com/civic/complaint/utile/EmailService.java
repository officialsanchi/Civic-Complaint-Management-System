package com.civic.complaint.utile;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(
            String email,
            String token) {

        String resetLink =
                "http://localhost:3000/reset-password?token="
                        + token;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Password Reset");
        message.setText(
                "Click the link below to reset your password:\n"
                        + resetLink);

        mailSender.send(message);
    }
}
