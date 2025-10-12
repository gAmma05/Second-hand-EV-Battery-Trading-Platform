package com.example.SWP.service.payment;

import com.example.SWP.exception.BusinessException;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomoService {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.returnUrl}")
    private String returnUrl;
    @Value("${momo.notifyUrl}")
    private String notifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createQrPayment(String orderId, long amount, String orderInfo, String extraData) {
        try {
            log.info("Creating MoMo QR payment for orderId={}, amount={}", orderId, amount);

            String requestId = UUID.randomUUID().toString();

            // Nếu extraData null → chuyển thành rỗng
            if (extraData == null) extraData = "";

            // Tạo chuỗi raw hash theo đúng thứ tự MoMo yêu cầu
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=payWithMethod";

            // Ký HMAC SHA256
            String signature = hmacSHA256(rawHash, secretKey);

            Map<String, Object> body = Map.ofEntries(
                    Map.entry("partnerCode", partnerCode),
                    Map.entry("partnerName", "SWP"),
                    Map.entry("storeId", "SWPStore"),
                    Map.entry("requestId", requestId),
                    Map.entry("amount", amount),
                    Map.entry("orderId", orderId),
                    Map.entry("orderInfo", orderInfo),
                    Map.entry("extraData", extraData),
                    Map.entry("redirectUrl", returnUrl),
                    Map.entry("ipnUrl", notifyUrl),
                    Map.entry("requestType", "payWithMethod"),
                    Map.entry("signature", signature)
            );

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> respBody = response.getBody();
            if (respBody == null || respBody.get("payUrl") == null) {
                log.error("Invalid response from MoMo: {}", response);
                throw new BusinessException("Invalid response from MoMo", 502);
            }

            String qrCodeUrl = (String) respBody.get("payUrl");
            log.info("MoMo QR payment created successfully: {}", qrCodeUrl);
            return qrCodeUrl;


        } catch (HttpClientErrorException ex) {
            log.error("MoMo API returned error: {}", ex.getResponseBodyAsString());
            throw new BusinessException("MoMo API error: " + ex.getResponseBodyAsString(), 502);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error creating MoMo QR payment: {}", e.getMessage(), e);
            throw new BusinessException("Internal error while creating MoMo QR payment", 500);
        }
    }


    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(result).toLowerCase();
    }
}
