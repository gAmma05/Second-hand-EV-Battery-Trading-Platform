package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerService {

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final PostRepository postRepository;

    public void createOrder(Authentication authentication, CreateOrderRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        if(user.getRole() != Role.BUYER) {
            throw new BusinessException("User is not a buyer", 400);
        }

        if(!postRepository.existsById(request.getPostId())) {
            throw new BusinessException("Post does not exist. You cannot create order on this post", 404);
        }


    }

}
