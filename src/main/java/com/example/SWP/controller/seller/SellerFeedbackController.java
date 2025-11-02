package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.feedback.FeedbackResponse;
import com.example.SWP.service.feedback.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/seller/feedbacks")
@RequiredArgsConstructor
public class SellerFeedbackController {

    FeedbackService feedbackService;

    @GetMapping("/")
    public ResponseEntity<?> getFeedbackFromOrder(Authentication authentication, @RequestParam Long orderId) {
        FeedbackResponse response = feedbackService.getFeedbackFromOrder(authentication, orderId);

        return ResponseEntity.ok(
                ApiResponse.<FeedbackResponse>builder()
                        .success(true)
                        .message("Feedback fetched successfully")
                        .data(response)
                        .build()
        );
    }
}
