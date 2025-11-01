package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.FeedbackRequest;
import com.example.SWP.entity.Feedback;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.FeedbackMapper;
import com.example.SWP.repository.FeedbackRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
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

    PostRepository postRepository;

    OrderRepository orderRepository;

    FeedbackMapper feedbackMapper;

    FeedbackRepository feedbackRepository;

    public void addFeedback(Authentication authentication, FeedbackRequest request) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new BusinessException("User not found", 404)
        );

        Post post = postRepository.findById(request.getPostId()).orElseThrow(
                () -> new BusinessException("Post not found", 404)
        );

        if (!Objects.equals(post.getStatus(), PostStatus.SOLD)) {
            throw new BusinessException("You cannot feedback as this product is not sold yet nor sold by you", 400);
        }

        Order order = orderRepository.findByPost_IdAndBuyer_IdAndStatus(post.getId(), user.getId(), OrderStatus.DONE);

        if (order == null) {
            throw new BusinessException("You cannot feedback as you have not ordered this product yet", 400);
        }

        Feedback feedback = feedbackMapper.toFeedback(request);
        feedback.setUser(user);
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }
}
