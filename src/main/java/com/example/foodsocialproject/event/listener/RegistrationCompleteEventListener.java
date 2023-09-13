package com.example.foodsocialproject.event.listener;

import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.event.RegistrationCompleteEvent;
import com.example.foodsocialproject.services.UserServices;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private final UserServices userService;
    private final JavaMailSender mailSender;
    private Users theUser;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        theUser = event.getUser();
        String verificationToken = UUID.randomUUID().toString();
        userService.saveUserVerificationToken(theUser, verificationToken);
        String url = event.getApplicationUrl()+"/register/verifyEmail?token="+verificationToken;
        try {
            sendVerificationEmail(theUser, url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendVerificationEmail(Users theUser, String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Xác Nhận Đăng ký";
        String senderName = "InnoTech";
        StringBuilder mailContentBuilder = new StringBuilder();
        mailContentBuilder.append("<html>");
        mailContentBuilder.append("<head>");
        mailContentBuilder.append("<style>");
        mailContentBuilder.append("body { font-family: Arial, sans-serif; }");
        mailContentBuilder.append("h3 { color: #383429; }");
        mailContentBuilder.append("h1 { color: #ffc31e; }");
        mailContentBuilder.append(".container { background-color: #ffffff;max-width: 400px;margin: 20px; padding: 20px; border: 3px solid #ffffff;border-radius: 10px;}");
    mailContentBuilder.append("p { margin-bottom: 10px; }");
    mailContentBuilder.append("a { color: #ffc31e; text-decoration: none; }");
    mailContentBuilder.append("</style>");
    mailContentBuilder.append("</head>");
    mailContentBuilder.append("<body style=\"background-color: #e1dada;padding:20px;border-radius: 10px;margin: 0 auto; max-width: fit-content;\">");
    mailContentBuilder.append("<div class=\"container\">");
    mailContentBuilder.append("<h1>InnoTech</h1><hr>");
    mailContentBuilder.append("<h3>Xin chào, " + theUser.getFullName() + "</h3>");
    mailContentBuilder.append("<p>Cám ơn bạn đã đăng ký tại khoản tại website chúng tôi. Để xác nhận việc đăng ký vui lòng ấn vòng đường link bên dưới:</p>");
    mailContentBuilder.append("<p>Hãy <a href=\"" + url + "\">xác nhận đăng ký tài khoản</a> của bạn, đường dẫn này sẽ hết hạn trong vòng <strong>15 phút</strong></p>");
    mailContentBuilder.append("<p>Xin cám ơn,<br>InnoTech</p>");
    mailContentBuilder.append("</div>");
    mailContentBuilder.append("</body>");
    mailContentBuilder.append("</html>");

    String mailContent = mailContentBuilder.toString();
    MimeMessage message = mailSender.createMimeMessage();
    var messageHelper = new MimeMessageHelper(message);
    messageHelper.setFrom("nghbang1909@gmail.com", senderName);
    System.out.println("Sending email to" + theUser.getEmail() + "...");
    messageHelper.setTo(theUser.getEmail());
    messageHelper.setSubject(subject);
    messageHelper.setText(mailContent, true);
    mailSender.send(message);
}
}
