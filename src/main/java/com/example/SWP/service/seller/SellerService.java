package com.example.SWP.service.seller;

import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;
import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerService {

    UserRepository userRepository;

    public void upgradeToSeller(Authentication authentication, UpgradeToSellerRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        if (user.getRole() == Role.SELLER) {
            throw new RuntimeException("User is already a seller");
        }

        user.setRole(Role.SELLER);
        user.setStoreName(request.getShopName());
        user.setStoreDescription(request.getShopDescription());
        user.setSocialMedia(request.getSocialMedia());
        userRepository.save(user);
    }
}
