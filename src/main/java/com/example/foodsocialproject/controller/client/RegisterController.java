package com.example.foodsocialproject.controller.client;

import com.example.foodsocialproject.dto.RegistrationRequest;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.entity.VerificationToken;
import com.example.foodsocialproject.event.RegistrationCompleteEvent;
import com.example.foodsocialproject.event.listener.RegistrationCompleteEventListener;
import com.example.foodsocialproject.exception.AlreadyExistsException;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.VerificationTokenRepository;
import com.example.foodsocialproject.services.UserServices;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final UserServices userService;
    private final ApplicationEventPublisher publisher;
    private final HttpServletRequest servletRequest;
    private final VerificationTokenRepository tokenRepository;
    private final RegistrationCompleteEventListener eventListener;

    @GetMapping("")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest("USER", "", "", "", ""));
        String url = applicationUrl(servletRequest) + "/login";
        model.addAttribute("url", url);
        return "client/auth-register";
    }

    @PostMapping("/process_register")
    public String processRegister(@Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest, BindingResult bindingResult, final HttpServletRequest request, Model model) {
        if (bindingResult.hasErrors()) {
            String url = applicationUrl(servletRequest) + "/login";
            model.addAttribute("url", url);
            return "client/auth-register";
        }
        try {
            registrationRequest = new RegistrationRequest("USER", registrationRequest.getPassword(),
                    registrationRequest.getFullName(), registrationRequest.getGender(),
                    registrationRequest.getEmail());
            Users user = userService.registerUser(registrationRequest);
            //publish registration event
            publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
            model.addAttribute("email", user.getEmail());
        } catch (AlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "client/auth-register";
        }
        model.addAttribute("successMessage", "Vui lòng kiểm tra địa chỉ email của bạn.");
        return "client/auth-register";
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }


    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        System.out.println("TOKEN: " + token);
        String url = applicationUrl(servletRequest)+"/register/resend-verification-token?token="+token;
        VerificationToken theToken = tokenRepository.findByToken(token);
        if (theToken==null) {
            model.addAttribute("error", "Không tìm thấy token của bạn.");
            return "client/auth-login";
        }
        if (theToken.getUser().isActive()) {
            model.addAttribute("success", "Tài khoản của bạn đã được kích hoạt.");
        } else {
            String verificationResult = userService.validateToken(token);
            if (verificationResult.equalsIgnoreCase("Valid")) {
                model.addAttribute("success", "Đăng ký thành công.");
            } else {
                model.addAttribute("error", "Đường dẫn này đã hết hạn, hãy <a style=\"color:#0b5ed7\" href=\"" + url + "\">lấy đường dẫn mới </a>để kích hoạt tài khoản của bạn!");
            }
        }
        return "client/auth-login";
    }

    @GetMapping("/resend-verification-token")
    public String resendVerificationToken(@RequestParam("token") String oldToken, final HttpServletRequest request, Model model) throws MessagingException, UnsupportedEncodingException {
        try {
            VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
            Users theUser = verificationToken.getUser();
            resendVerificationTokenEmail(theUser, applicationUrl(request), verificationToken);
            model.addAttribute("success", "Vui lòng kiểm tra liên kiết vừa được gửi về email của bạn.");

        } catch (ResourceNotFoundException e) {
            model.addAttribute("title", "Lỗi");
            model.addAttribute("message", "Có lỗi xảy ra trong quá trình tìm kiếm token !.");
        }
        return "client/auth-login";
    }

    private void resendVerificationTokenEmail(Users theUser, String applicationUrl, VerificationToken verificationToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationUrl+"/register/verifyEmail?token="+verificationToken.getToken();
        eventListener.sendVerificationEmail(theUser, url);
    }
}
