package com.example.SWP.controller.buyer;

import com.example.SWP.dto.request.buyer.FeedbackRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.feedback.FeedbackResponse;
import com.example.SWP.service.buyer.BuyerFeedbackService;
import com.example.SWP.service.feedback.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buyer/feedbacks")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerFeedbackController {

    BuyerFeedbackService buyerFeedbackService;

    FeedbackService feedbackService;

    @PostMapping("/create-feedback")
    public ResponseEntity<?> createFeedback(Authentication authentication, @Valid @RequestBody FeedbackRequest request) {
        buyerFeedbackService.addFeedback(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Tạo feedback thành công")
                        .build()
        );
    }

    @GetMapping("/")
    public ResponseEntity<?> getFeedbackFromOrder(Authentication authentication, @RequestParam Long orderId) {
        FeedbackResponse response = feedbackService.getFeedbackFromOrder(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<FeedbackResponse>builder()
                        .success(true)
                        .message("Truy xuất feedback thành công")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteFeedback(Authentication authentication, @RequestParam Long feedbackId) {
        buyerFeedbackService.deleteFeedback(authentication, feedbackId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Đã xóa feedback")
                        .build()
        );
    }

}
