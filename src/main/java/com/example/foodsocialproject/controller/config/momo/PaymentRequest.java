package com.example.foodsocialproject.controller.config.momo;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaymentRequest {
    public PaymentRequest() {
    }

    public static String sendPaymentRequest(String endpoint, String postJsonString) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes(postJsonString);
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            StringBuilder jsonResponse = new StringBuilder();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponse.append(line);
                    }
                }
            }

            return jsonResponse.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
