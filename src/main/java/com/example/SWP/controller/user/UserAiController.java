package com.example.SWP.controller.user;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/ai")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)

public class UserAiController {

    AiService aiService;

    @PostMapping("/suggest-price")
    public double suggestPrice(@RequestBody AiProductRequest request) {
        return aiService.suggestPrice(request);
    }
}
