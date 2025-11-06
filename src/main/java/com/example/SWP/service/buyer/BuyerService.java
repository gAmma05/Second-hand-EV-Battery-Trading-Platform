package com.example.SWP.service.buyer;

import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerService {
    ValidateService validateService;
    UserRepository userRepository;

    public void changeToBuyer(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("Chỉ người bán mới có thể chuyển về người mua", 400);
        }
        user.setRole(Role.BUYER);
        userRepository.save(user);
    }
}
