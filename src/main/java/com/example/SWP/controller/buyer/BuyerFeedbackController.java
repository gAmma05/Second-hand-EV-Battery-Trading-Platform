package com.example.SWP.controller.buyer;

import com.example.SWP.service.buyer.BuyerFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buyer/feedbacks")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerFeedbackController {

    BuyerFeedbackService buyerFeedbackService;

    public ResponseEntity<?> getFeedbacks(Long sellerId) {
        return null;
    }

}
