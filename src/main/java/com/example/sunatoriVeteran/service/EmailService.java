package com.example.sunatoriVeteran.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code) {
        System.out.println("\n=======================================================");
        System.out.println("VERIFICATION CODE FOR " + toEmail + " IS: " + code);
        System.out.println("=======================================================\n");

        if (mailSender == null) {
            System.out.println("MailSender is not configured. Email won't be sent.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("benlain.wap@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Підтвердження реєстрації на BookVeteran.ua");
            message.setText("Вітаємо!\n\nВаш код для підтвердження реєстрації: " + code + "\n\nНікому не передавайте цей код.");
            
            mailSender.send(message);
            System.out.println("Email successfully sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Помилка при відправці email (можливо не налаштовано SMTP): " + e.getMessage());
        }
    }
}
