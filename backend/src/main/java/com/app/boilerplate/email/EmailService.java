package com.app.boilerplate.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${app.mail.from:noreply@example.com}")
    private String defaultFrom;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendHtml(String to, String subject, String html) throws MessagingException {
        String fromAddress = (from != null && !from.isBlank()) ? from : defaultFrom;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    public void sendTemplate(String to, String subject, String templateName, Map<String, Object> model) throws MessagingException {
        Context context = new Context();
        if (model != null) {
            model.forEach(context::setVariable);
        }
        String html = templateEngine.process(templateName, context);
        sendHtml(to, subject, html);
    }

    public void sendWelcome(String to, String name, String appUrl) throws MessagingException {
        sendTemplate(to, "Welcome", "email/welcome", Map.of(
                "name", name != null ? name : "User",
                "appUrl", appUrl != null ? appUrl : "http://localhost:5173"
        ));
    }

    public void sendPasswordReset(String to, String resetUrl, String expiresIn) throws MessagingException {
        sendTemplate(to, "Reset Your Password", "email/password-reset", Map.of(
                "resetUrl", resetUrl != null ? resetUrl : "",
                "expiresIn", expiresIn != null ? expiresIn : "1 hour"
        ));
    }

    public void sendVerifyEmail(String to, String verifyUrl, String expiresIn) throws MessagingException {
        sendTemplate(to, "Verify Your Email", "email/verify-email", Map.of(
                "verifyUrl", verifyUrl != null ? verifyUrl : "",
                "expiresIn", expiresIn != null ? expiresIn : "24 hours"
        ));
    }
}
