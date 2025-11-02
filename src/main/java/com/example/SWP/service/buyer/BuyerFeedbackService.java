package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.FeedbackRequest;
import com.example.SWP.entity.Feedback;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.FeedbackMapper;
import com.example.SWP.repository.FeedbackRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BuyerFeedbackService {

    UserRepository userRepository;

    OrderRepository orderRepository;

    FeedbackMapper feedbackMapper;

    FeedbackRepository feedbackRepository;

    public void addFeedback(Authentication authentication, FeedbackRequest request) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new BusinessException("Không tìm thấy thông tin user", 404)
        );

        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new BusinessException("Không tìm thấy order", 404)
        );

        if (!Objects.equals(order.getBuyer().getId(), user.getId())) {
            throw new BusinessException("Order này không phải của bạn", 400);
        }

        if (!Objects.equals(order.getStatus(), OrderStatus.DONE)) {
            throw new BusinessException("Đơn hàng của bạn chưa được giao hoặc bạn chưa nhận, bạn không thể feedback trên order này", 400);
        }

        Feedback feedback = feedbackMapper.toFeedback(request);
        feedback.setUser(user);
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }
}
