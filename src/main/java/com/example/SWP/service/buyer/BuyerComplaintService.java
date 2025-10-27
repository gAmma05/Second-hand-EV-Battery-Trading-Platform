package com.example.SWP.service.buyer;

import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerComplaintService {

    OrderDeliveryRepository orderDeliveryRepository;

    UserRepository userRepository;

    public void createComplaint(Authentication authentication, Long orderId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrderId(orderId);

        if(!Objects.equals(orderDelivery.getOrder().getBuyer().getId(), user.getId())){
            throw new BusinessException("This order is not your", 400);
        }

        if(!Objects.equals(orderDelivery.getStatus(), DeliveryStatus.RECEIVED)){
            throw new BusinessException("Failed to create complaint, could be not yet deliver or receive", 400);
        }


    }
}
