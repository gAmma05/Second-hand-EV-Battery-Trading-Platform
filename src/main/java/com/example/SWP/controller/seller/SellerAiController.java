package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/ai")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)

public class SellerAiController {

    AiService aiService;

    @PostMapping("/suggest-price")
    public Map<String, Object> suggestPrice(@RequestBody AiProductRequest request) {
        double suggestedPrice = aiService.suggestPrice(request);

        Map<String, Object> response = new HashMap<>();
        response.put("suggestedPrice", suggestedPrice);

        return response;
    }

}
