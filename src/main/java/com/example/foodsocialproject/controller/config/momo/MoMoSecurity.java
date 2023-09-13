package com.example.foodsocialproject.controller.config.momo;

import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class MoMoSecurity {
    public MoMoSecurity() {
        // Encrypt and decrypt password using secure
    }

    public String getHash(String partnerCode, String merchantRefId,
                          String amount, String paymentCode, String storeId, String storeName, String publicKeyBase64) throws Exception {
        String json = "{\"partnerCode\":\"" +
                partnerCode + "\",\"partnerRefId\":\"" +
                merchantRefId + "\",\"amount\":" +
                amount + ",\"paymentCode\":\"" +
                paymentCode + "\",\"storeId\":\"" +
                storeId + "\",\"storeName\":\"" +
                storeName + "\"}";

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String result = null;
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64Utils.decodeFromString(publicKeyBase64)));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(data);
            result = Base64Utils.encodeToString(encryptedData);
        } catch (Exception ex) {
            // Handle encryption error
        }
        return result;
    }

    public String buildQueryHash(String partnerCode, String merchantRefId,
                                 String requestId, String publicKeyBase64) throws Exception {
        String json = "{\"partnerCode\":\"" +
                partnerCode + "\",\"partnerRefId\":\"" +
                merchantRefId + "\",\"requestId\":\"" +
                requestId + "\"}";

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String result = null;
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64Utils.decodeFromString(publicKeyBase64)));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(data);
            result = Base64Utils.encodeToString(encryptedData);
        } catch (Exception ex) {
            // Handle encryption error
        }
        return result;
    }

    public String buildRefundHash(String partnerCode, String merchantRefId,
                                  String momoTransId, long amount, String description, String publicKeyBase64) throws Exception {
        String json = "{\"partnerCode\":\"" +
                partnerCode + "\",\"partnerRefId\":\"" +
                merchantRefId + "\",\"momoTransId\":\"" +
                momoTransId + "\",\"amount\":" +
                amount + ",\"description\":\"" +
                description + "\"}";

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String result = null;
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64Utils.decodeFromString(publicKeyBase64)));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(data);
            result = Base64Utils.encodeToString(encryptedData);
        } catch (Exception ex) {
            // Handle encryption error
        }
        return result;
    }

    public String signSHA256(String message, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hashBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}