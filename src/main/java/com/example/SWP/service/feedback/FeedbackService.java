package com.example.SWP.service.feedback;

import com.example.SWP.dto.response.feedback.FeedbackResponse;
import com.example.SWP.entity.Feedback;
import com.example.SWP.entity.User;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.FeedbackMapper;
import com.example.SWP.repository.FeedbackRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FeedbackService {

    FeedbackRepository feedbackRepository;

    FeedbackMapper feedbackMapper;

    UserRepository userRepository;

    public FeedbackResponse getFeedbackFromOrder(Authentication authentication, Long orderId) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new BusinessException("Không tìm thấy user", 404));

        Feedback feedback = feedbackRepository.findByOrder_Id(orderId);

        if (feedback == null) {
            return null;
        }

        if (!Objects.equals(feedback.getOrder().getSeller().getId(), user.getId()) &&
                !Objects.equals(feedback.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("Order này không phải của bạn, bạn không thể xem feedback", 400);
        }

        return feedbackMapper.toFeedbackResponse(feedback);
    }
}
