package com.example.foodsocialproject.controller.client;

import com.example.foodsocialproject.entity.Orders;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.services.*;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    @Autowired
    private UserServices userServices;
    @Autowired
    private OrderService orderService;
    @Autowired
    private EmailSenderService emailSenderService;
    @Autowired
    private PaypalService paypalService;
    public static final String SUCCESS_URL = "paypal/success";
    public static final String CANCEL_URL = "paypal/cancel";

/*    public static final String ACCOUNT_SID = "ACa3f5ab465b8859f75c2294541894d897";
    public static final String AUTH_TOKEN = "83aeb324a9d29ddead76c429088e762b";
    public static final String TWILIO_PHONE_NUMBER = "+13156303801";*/

    @GetMapping()
    public String showCart(HttpSession session, @NotNull Model model) {
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalPrice", cartService.getSumPrice(session));
        model.addAttribute("totalQuantity", cartService.getSumQuantity(session));
        session.setAttribute("totalItems", cartService.getSumQuantity(session));
        return "client/cart/index";
    }
    @GetMapping("/clearCart")
    public String clearCart(HttpSession session) {
        cartService.removeCart(session);
        return "redirect:/cart";
    }
    @GetMapping("/checkout")
    public String checkoutView(Model model, HttpSession session){
        if(model.containsAttribute("message")){
            model.addAttribute("message", model.getAttribute("message"));
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users user = userServices.findbyEmail(email).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalPrice", cartService.getSumPrice(session));
        return "client/cart/checkout";
    }
    @PostMapping("/checkout")
    public String checkout(@RequestParam("address") String address,
                           @RequestParam("phone") String phone,
                           @RequestParam(name = "note", defaultValue = "") String note,
                           @RequestParam(name = "cash", defaultValue = "false") boolean cash,
                           @RequestParam(name = "paypal",defaultValue = "false") boolean paypal, HttpSession session,
                           RedirectAttributes redirectAttributes){

        if(cash == true  && paypal == true){
            redirectAttributes.addFlashAttribute("message", "message");
            return "redirect:/cart/checkout";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users user = userServices.findbyEmail(email).orElseThrow();
        session.setAttribute("note", note);
        session.setAttribute("address",address);
        session.setAttribute("phone",phone);
        if(cash == true){
            cartService.saveCart(session,note,address,user,"cash", false);
            return "redirect:/cart/success";
        }
        if(paypal == true){
            return "redirect:/cart/paypal_checkout";
        }
        redirectAttributes.addFlashAttribute("message", "message");
        return "redirect:/cart/checkout";
    }
    @GetMapping("/paypal_checkout")
    public String paypalCheckout(HttpSession session){
        try {
            //get price
            double totalMoney = cartService.getSumPrice(session);
            double USD  = totalMoney / 23000;;
            Payment payment = paypalService.createPayment(USD, "USD",
                    "paypal","sale","FoodSocialNetWork",
                    "http://localhost:8080/cart/" + CANCEL_URL,
                    "http://localhost:8080/cart/" + SUCCESS_URL);
            for(Links link:payment.getLinks()) {
                if(link.getRel().equals("approval_url")) {
                    return "redirect:"+link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:/cart/checkout";
    }
    @GetMapping(value = SUCCESS_URL)
    public String paypalSuccess(@RequestParam("paymentId") String paymentId,
                                @RequestParam("PayerID") String payerId, HttpSession session){
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                //get user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();
                Users user = userServices.findbyEmail(email).orElseThrow();
                //save
                String note =(String)session.getAttribute("note");
                String address = (String)session.getAttribute("address");
                cartService.saveCart(session,note,address,user,"paypal", true);
                return "redirect:/cart/success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/cart/checkout";
    }    @GetMapping(value =CANCEL_URL)
    public String checkoutCancel(){
        return "client/cart/checkout-cancel";
    }
    @GetMapping("/success")
    public String checkoutSuccess(HttpSession session) throws MessagingException, UnsupportedEncodingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users user = userServices.findbyEmail(email).orElseThrow();
        //get order information
        Long orderID = (Long) session.getAttribute("orderID");
        Orders order = orderService.getOrderById(orderID);
        Double sumPrice = (Double) session.getAttribute("sumPrice");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String method;
        if(order.isPayment())
            method = "Paypal";
        else
            method = "Thanh toán khi nhận hàng";

        String subject = "Order Confirmation - Order number: #" + orderID;

        String body = "<h3>Hello, " + user.getFullName()+ "</h3>";
        body += "Mã đơn hàng: #"+orderID;
        body += "<p>Ngày đặt hàng: "+ dateFormat.format(order.getDate_purchase())+"</p>";
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat vn = NumberFormat.getInstance(localeVN);
        String str2 = vn.format(sumPrice);
        body += "<p style=\"margin-top: 20px\">Tổng tiền: "+ "<span style=\"font-weight: bold\">"+ str2 +" </span>" +"</p>";
        body += "<p>Phương thức thanh toán: "+method+"</p>";
        body += "<h3 style=\"margin-top: 20px\">Địa chỉ giao hàng: </h3>" +
                "<p>" +user.getFullName()+"</p>" +
                "<p>" +order.getAddress() +"</p>" +
                "<p>" +user.getPhone()+ "</p>";
        body += "<h3 style=\"margin-top: 20px\">Cảm ơn vì đã chọn sản phẩm của chúng tôi!!</h3>";

        emailSenderService.sendEmail(email,subject,body);

        //send sms
        String destinyPhone = (String) session.getAttribute("phone");
      /* if (destinyPhone != null) {
            if (validatePhoneNumber(destinyPhone)) {
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                System.out.println("sms sending to "+ destinyPhone);
                String message = "Cảm ơn vì đã chọn sản phẩm của chúng tôi. Bạn có mã đơn hàng: " + orderID + ",  đặt ngày: " + dateFormat.format(order.getDate_purchase()) + ". Hình thức thanh toán Paypal với tổng tiền " + str2;
                // Sử dụng thư viện Twilio để gửi tin nhắn SMS
                Message.creator(
                        new PhoneNumber(destinyPhone), // Số điện thoại người nhận (+84 + số điện thoại)
                        new PhoneNumber(TWILIO_PHONE_NUMBER), // Số điện thoại nguồn (Twilio phone number)
                        message// Nội dung tin nhắn
                ).create();
            }
        }*/
        return "client/cart/checkout-success";
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        // Định dạng pattern
        String pattern = "(\\+84)\\d{9,10}";

        // Tạo đối tượng Pattern từ pattern
        Pattern compiledPattern = Pattern.compile(pattern);

        // Tạo đối tượng Matcher để so khớp pattern với số điện thoại
        Matcher matcher = compiledPattern.matcher(phoneNumber);

        // Kiểm tra khớp pattern
        return matcher.matches();
    }
}
