package com.example.SWP.service.seller;

import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.*;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerService {

    UserRepository userRepository;
    PriorityPackageRepository priorityPackageRepository;
    SellerPackageRepository sellerPackageRepository;
    ValidateService validateService;

    public void upgradeToSeller(Authentication authentication, UpgradeToSellerRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        if (checkIfSeller(user)) {
            throw new BusinessException("Người dùng hiện đã nâng cấp thành người bán", 400);
        }

        user.setRole(Role.SELLER);
        user.setStoreName(request.getShopName());

        if(request.getShopDescription() != null) {
            user.setStoreDescription(request.getShopDescription());
        }

        if(request.getSocialMedia() != null) {
            user.setSocialMedia(request.getSocialMedia());
        }

        user.setRemainingBasicPosts(3);
        userRepository.save(user);
    }


    public void changeToSeller(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        if(!checkIfSeller(user)) {
            throw new BusinessException("Người dùng hiện chưa nâng cấp thành người bán", 400);
        }

        user.setRole(Role.SELLER);
        userRepository.save(user);
    }

    public boolean upgradedToSeller(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);
        return  checkIfSeller(user);
    }

    public boolean checkIfSeller(User user) {
        return user.getStoreName() != null;
    }


    public List<PriorityPackage> getAllPriorityPackages() {
        return priorityPackageRepository.findAll();
    }

    public List<SellerPackage> getAllSellerPackages() {
        return sellerPackageRepository.findAll();
    }
}
