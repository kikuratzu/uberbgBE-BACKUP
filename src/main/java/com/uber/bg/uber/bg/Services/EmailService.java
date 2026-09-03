package com.uber.bg.uber.bg.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.StreamUtils;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    public EmailService(JavaMailSender mailSender, ResourceLoader resourceLoader) {
        this.mailSender = mailSender;
        this.resourceLoader = resourceLoader;
    }
    @Retryable(
            retryFor = { MessagingException.class, IOException.class },
            maxAttempts = 4,
            backoff = @Backoff(delay = 2000, multiplier = 2.0) // 2s, then 4s, then 8s
    )
    public void sendVerificationCode(String to, String subject, String sixDigitCode) {
        try {
            Resource resource = resourceLoader.getResource("classpath:/2fa-template.html");
            String htmlTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);


            String finalizedHtml = htmlTemplate.replace("[VERIFICATION_CODE]", sixDigitCode);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(finalizedHtml, true);

            mailSender.send(message);

        } catch (IOException | MessagingException e) {
            throw new IllegalStateException("Failed to parse or send verification email layout", e);
        }
    }
}

