package com.example.SWP.service.buyer;

import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BuyerFeedbackService {

    UserRepository userRepository;

    PostRepository postRepository;

    OrderRepository orderRepository;

    public void addFeedback(Authentication authentication, Long postId) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new BusinessException("User not found", 404)
        );

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("Post not found", 404)
        );

        if(!Objects.equals(post.getStatus(), PostStatus.SOLD)){
            throw new BusinessException("You cannot feedback as this product is not sold yet nor sold by you", 400);
        }

        Order order = orderRepository.findByPost_IdAndBuyer_IdAndStatus(postId, user.getId(), OrderStatus.DONE);


    }
}
