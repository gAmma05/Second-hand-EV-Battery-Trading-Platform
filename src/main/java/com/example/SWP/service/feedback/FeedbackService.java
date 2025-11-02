package com.example.SWP.service.feedback;

import com.example.SWP.dto.response.feedback.FeedbackResponse;
import com.example.SWP.entity.Feedback;
import com.example.SWP.mapper.FeedbackMapper;
import com.example.SWP.repository.FeedbackRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FeedbackService {

    FeedbackRepository feedbackRepository;

    FeedbackMapper feedbackMapper;

    public FeedbackResponse getFeedback(Long orderId) {
        Feedback feedback = feedbackRepository.findByOrder_Id(orderId);

        if (feedback == null) {
            return null;
        }

        return feedbackMapper.toFeedbackResponse(feedback);
    }
}
