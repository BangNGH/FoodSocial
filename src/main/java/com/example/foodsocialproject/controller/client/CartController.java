package com.example.foodsocialproject.controller.client;

import com.example.foodsocialproject.controller.config.momo.MoMoSecurity;
import com.example.foodsocialproject.controller.config.momo.PaymentRequest;
import com.example.foodsocialproject.controller.config.vnpay.Config;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
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
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
                           @RequestParam(name = "payOption", defaultValue = "false") String payment,
                           HttpSession session,
                           RedirectAttributes redirectAttributes,final HttpServletRequest request) throws Exception {
        double totalMoney = cartService.getSumPrice(session);

        if(payment.trim().length()>6){
            redirectAttributes.addFlashAttribute("message", "Có lỗi khi chọn phương thức thanh toán");
            return "redirect:/cart/checkout";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users user = userServices.findbyEmail(email).orElseThrow();
        session.setAttribute("note", note);
        session.setAttribute("address",address);
        session.setAttribute("phone",phone);
        if(payment.equals("cash")){
            cartService.saveCart(session,note,address,user,"cash", false);
            return "redirect:/cart/success";
        }
        if(payment.equals("paypal")){
            return "redirect:/cart/paypal_checkout";
        }
        if( payment.equals("momo")){
            System.out.println("Total: " + totalMoney);
            String momoAmount = String.valueOf(totalMoney);
            String sub_momoAmount = momoAmount.substring(0, momoAmount.length() - 2);
            String momoPaymentUrl = paymentMomo(sub_momoAmount,request);
            return "redirect:"+momoPaymentUrl;
        }
        if( payment.equals("vnpay")){
            long vnpay_Amount = (long) (totalMoney * 100);
            String vnpayPaymentUrl = paymentVnpay(vnpay_Amount, request);
            return "redirect:"+vnpayPaymentUrl;
        }
        redirectAttributes.addFlashAttribute("message", "message");
        return "redirect:/cart/checkout";
    }
    @GetMapping("/paypal_checkout")
    public String paypalCheckout(HttpSession session, HttpServletRequest request){
        try {
            //get price
            double totalMoney = cartService.getSumPrice(session);
            double USD  = totalMoney / 23000;;
            Payment payment = paypalService.createPayment(USD, "USD",
                    "paypal","sale","FoodSocialNetWork",
                    applicationUrl(request)+"/cart/"+ CANCEL_URL,
                    applicationUrl(request)+"/cart/" + SUCCESS_URL);
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
    }
    @GetMapping(value =CANCEL_URL)
    public String checkoutCancel(){
        return "client/cart/checkout-cancel";
    }

    public String checkoutSuccess(HttpSession session, String method) throws MessagingException, UnsupportedEncodingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users user = userServices.findbyEmail(email).orElseThrow();
        //get order information
        Long orderID = (Long) session.getAttribute("orderID");
        Orders order = orderService.getOrderById(orderID);
        Double sumPrice = (Double) session.getAttribute("sumPrice");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


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


    @GetMapping("/success")
    public String checkoutSuccess(HttpSession session) throws MessagingException, UnsupportedEncodingException {
        String method;
        method = "Paypal";
        return checkoutSuccess(session,method);

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

    public String paymentMomo(String send_amount, HttpServletRequest request) throws Exception {

        // Request params needed to request MoMo system
        String endpoint = "https://test-payment.momo.vn/gw_payment/transactionProcessor";
        String partnerCode = "MOMOOJOI20210710";
        String accessKey = "iPXneGmrJH0G8FOP";
        String serectkey = "sFcbSGRSJjwGxwhhcEktCHWYUuTuPNDB";

/*        String endpoint ="https://test-payment.momo.vn/v2/gateway/api/create";
        String partnerCode = "MOMOBKUN20180529";
        String accessKey = "klm05TvNBzhg7h7j";
        String serectkey = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";*/

        String orderInfo = "Payment";
        String returnUrl = applicationUrl(request) + "/cart/momo-payment-result";
        String notifyUrl = "https://d796-101-99-32-135.ngrok-free.app/home";
        String amount = send_amount;
        String orderId = String.valueOf(System.currentTimeMillis());
        String requestId = String.valueOf(System.currentTimeMillis());
        String extraData = "";

        // Before sign HMAC SHA256 signature
        String rawHash = "partnerCode=" +
                partnerCode + "&accessKey=" +
                accessKey + "&requestId=" +
                requestId + "&amount=" +
                amount + "&orderId=" +
                orderId + "&orderInfo=" +
                orderInfo + "&returnUrl=" +
                returnUrl + "&notifyUrl=" +
                notifyUrl + "&extraData=" +
                extraData;

        MoMoSecurity crypto = new MoMoSecurity();
        // Sign signature SHA256
        String signature = crypto.signSHA256(rawHash, serectkey);

        // Build body JSON request
        JSONObject message = new JSONObject();
        message.put("partnerCode", partnerCode);
        message.put("accessKey", accessKey);
        message.put("requestId", requestId);
        message.put("amount", amount);
        message.put("orderId", orderId);
        message.put("orderInfo", orderInfo);
        message.put("returnUrl", returnUrl);
        message.put("notifyUrl", notifyUrl);
        message.put("extraData", extraData);
        message.put("requestType", "captureMoMoWallet");
        message.put("signature", signature);

        System.out.println("Calling MomoWallet Api... ");
        String responseFromMomo = PaymentRequest.sendPaymentRequest(endpoint, message.toString());
        JSONObject jmessage = new JSONObject(responseFromMomo);
        String payUrl = jmessage.getString("payUrl");
        return payUrl;
    }
    private String paymentVnpay(long send_amount, HttpServletRequest request) throws UnsupportedEncodingException {
        //Thanh toán VNPAY
        long amount = send_amount;
        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        String vnp_IpAddr = Config.getIpAddress(request);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        // vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", String.valueOf(System.currentTimeMillis()));
        vnp_Params.put("vnp_OrderType", "billpayment");
        vnp_Params.put("vnp_Locale", "vn");

        String vnp_Returnurl = applicationUrl(request) + "/cart/vnpay-payment-result";
        vnp_Params.put("vnp_ReturnUrl", vnp_Returnurl);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;
        System.out.println("Called: " + paymentUrl);
        return paymentUrl;
    }

    @GetMapping("/momo-payment-result")
    public String momoPaymentResult( @RequestParam("errorCode") String errorCode,HttpSession session,RedirectAttributes redirectAttributes
                                 ) throws MessagingException, UnsupportedEncodingException {
        if (errorCode.equals("0")) {
        return checkoutSuccess(session,"Momo");}
        redirectAttributes.addFlashAttribute("message", "Có lỗi xảy ra khi thanh toán");
        return "redirect:/cart/checkout";
    }
    @GetMapping("/vnpay-payment-result")
    public String showResult(@RequestParam("vnp_ResponseCode") String responseCode, HttpSession session,RedirectAttributes redirectAttributes
    ) throws MessagingException, UnsupportedEncodingException {
        if (responseCode.equals("00")) {
            return checkoutSuccess(session,"VNPay");
        }
        redirectAttributes.addFlashAttribute("message", "Có lỗi xảy ra khi thanh toán");
        return "redirect:/cart/checkout";
    }
    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
