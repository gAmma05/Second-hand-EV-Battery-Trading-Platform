package com.example.SWP.service.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VnPayService {

    @Value("${vnpay.tmnCode}")
    String tmnCode;
    @Value("${vnpay.hashSecret}")
    String hashSecret;
    @Value("${vnpay.payUrl}")
    String payUrl;

    public String createPaymentUrl(String orderId, int amount, String description, String returnUrl) {
        try {
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", tmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", orderId);
            vnp_Params.put("vnp_OrderInfo", description);
            vnp_Params.put("vnp_OrderType", "billpayment");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", "127.0.0.1");
            vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            cld.add(Calendar.MINUTE, 15);
            vnp_Params.put("vnp_ExpireDate", new SimpleDateFormat("yyyyMMddHHmmss").format(cld.getTime()));

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }

            String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            return payUrl + "?" + query;

        } catch (Exception e) {
            throw new RuntimeException("Error while creating URL VNPAY: " + e.getMessage());
        }
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            final SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating HMAC SHA512", e);
        }
    }
}
