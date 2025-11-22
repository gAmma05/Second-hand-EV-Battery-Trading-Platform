package com.example.SWP.service.ai;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.dto.response.AiValidationResult;
import com.example.SWP.enums.ProductType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiService {

    ChatClient chatClient;
    AiBuildPromptService aiBuildPromptService;

    public double suggestPrice(AiProductRequest request) {
        AiValidationResult validate = validateProduct(request);

        if(!validate.isValid()) {
            return 0.0;
        }

        String prompt = aiBuildPromptService.buildPromptSuggestPrice(request);

        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            return 0.0;
        }

        try {
            String numeric = response.replaceAll("[^0-9.]", "");
            return numeric.isEmpty() ? 0.0 : Double.parseDouble(numeric);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public AiValidationResult validateProduct(AiProductRequest request) {
        String prompt = aiBuildPromptService.buildPromptValidateProduct(request);

        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            return AiValidationResult.builder()
                    .isValid(false)
                    .reason("Lỗi hệ thống: AI không phản hồi.")
                    .build();
        }

        String cleanResponse = response.trim();

        // TRƯỜNG HỢP 3: AI TRẢ PHẢN HỒI BÀI ĐĂNG HỢP LỆ
        if (cleanResponse.equalsIgnoreCase("Valid")) {
            return AiValidationResult.builder()
                    .isValid(true)
                    .reason("Sản phẩm hợp lệ.")
                    .build();
        }

        // TRƯỜNG HỢP 3: AI TRẢ PHẢN HỒI BÀI ĐĂNG KHÔNG HỢP LỆ
        // AI trả về dạng "Invalid: <Lý do>"
        if (cleanResponse.startsWith("Invalid:")) {
            // Cắt bỏ chữ "Invalid:" (độ dài là 8) để lấy nội dung phía sau
            String reason = cleanResponse.substring("Invalid:".length()).trim();
            return AiValidationResult.builder()
                    .isValid(false)
                    .reason(reason)
                    .build();
        }

        // TRƯỜNG HỢP 3: AI TRẢ LỜI LINH TINH
        return AiValidationResult.builder()
                .isValid(false)
                .reason("Phản hồi không xác định từ AI: " + cleanResponse)
                .build();
    }

    public String compareProduct(AiProductRequest request1, AiProductRequest request2) {
        String prompt = aiBuildPromptService.buildPromptCompare(request1, request2);
        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            return "No comparison result available.";
        }
        return response.trim();
    }
}
