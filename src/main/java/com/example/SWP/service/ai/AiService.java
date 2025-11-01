package com.example.SWP.service.ai;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
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
        String prompt = aiBuildPromptService.buildPromptSuggestPrice(request);

        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            return 0.0;
        }

        try {
            System.out.println("AI response (price): " + response);
            String numeric = response.replaceAll("[^0-9.]", "");
            return numeric.isEmpty() ? 0.0 : Double.parseDouble(numeric);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean validateProduct(AiProductRequest request) {
        String prompt = aiBuildPromptService.buildPromptValidateProduct(request);
        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            return false;
        }

        response = response.trim().toLowerCase();

        return response.equals("valid");
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
