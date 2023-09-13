package com.example.foodsocialproject.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender;
    public void sendEmail(String toEmail, String subject, String body) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("nghbang1909@gmail.com", "InnoTech");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        body += "<hr><img src ='cid:logoImage' />";
        helper.setText(body, true);
        ClassPathResource resource = new ClassPathResource("/static/client_assets/images/logo/logo.png");
        helper.addInline("logoImage", resource) ;
        mailSender.send(message);
    }
}
