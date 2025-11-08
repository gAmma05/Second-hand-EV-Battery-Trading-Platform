package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.FeedbackRequest;
import com.example.SWP.entity.Feedback;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.FeedbackMapper;
import com.example.SWP.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BuyerFeedbackService {

    UserRepository userRepository;

    OrderRepository orderRepository;

    FeedbackMapper feedbackMapper;

    FeedbackRepository feedbackRepository;

    OrderDeliveryRepository orderDeliveryRepository;

    ComplaintRepository complaintRepository;

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

        if (complaintRepository.countComplaintByOrderIdAndStatus(request.getOrderId(), ComplaintStatus.RESOLVING) > 0
                || complaintRepository.countComplaintByOrderIdAndStatus(request.getOrderId(), ComplaintStatus.ADMIN_SOLVING) > 0
                || complaintRepository.countComplaintByOrderIdAndStatus(request.getOrderId(), ComplaintStatus.REJECTED) > 0) {
            throw new BusinessException("Đơn hàng này đang có khiếu nại, chưa thể feedback", 400);
        }

        checkCurrentFeedback(request.getOrderId());

        Feedback feedback = feedbackMapper.toFeedback(request);
        feedback.setUser(user);
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }

    private void checkCurrentFeedback(Long orderId) {
        Optional<Feedback> feedbackOptional = feedbackRepository.findByOrder_Id(orderId);
        if (feedbackOptional.isPresent()) {
            throw new BusinessException("Bạn đã feedback trên cái order này rồi, bạn hãy xóa nếu muốn feedback bằng nội dung khác", 400);
        }
    }

    public void deleteFeedback(Authentication authentication, Long feedbackId) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new BusinessException("Không tìm thấy thông tin user", 404)
        );

        Optional<Feedback> feedbackOptional = feedbackRepository.findById(feedbackId);
        if (feedbackOptional.isEmpty()) {
            throw new BusinessException("Không tìm thấy feedback của order này để xóa, thử F5 lại nha", 404);
        }

        Feedback feedback = feedbackOptional.get();
        if (!Objects.equals(feedback.getUser().getId(), user.getId())) {
            throw new BusinessException("Feedback này không phải của bạn, bạn không thể xóa", 400);
        }

        feedbackRepository.deleteById(feedback.getId());
    }
}
