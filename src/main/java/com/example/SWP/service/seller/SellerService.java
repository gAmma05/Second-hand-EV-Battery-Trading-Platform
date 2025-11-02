package com.example.SWP.service.seller;

import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.*;
import com.example.SWP.service.ghn.GhnService;
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
    GhnService ghnService;


    public void upgradeToSeller(Authentication authentication, UpgradeToSellerRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        if (user.getRole() == Role.SELLER) {
            throw new BusinessException("Người dùng đã là seller", 400);
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new BusinessException("Vui lòng cập nhật họ tên đầy đủ (full name) trước khi đăng ký trở thành seller", 400);
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new BusinessException("Vui lòng cập nhật số điện thoại (phone number) trước khi đăng ký trở thành seller", 400);
        }
        if (user.getAddress() == null) {
            throw new BusinessException("Vui lòng cập nhật địa chỉ (address) trước khi đăng ký trở thành seller", 400);
        }

        user.setRole(Role.SELLER);
        user.setStoreName(request.getShopName());
        user.setStoreDescription(request.getShopDescription());
        user.setSocialMedia(request.getSocialMedia());
        user.setRemainingBasicPosts(3);
        userRepository.save(user);
    }

    public List<PriorityPackage> getAllPriorityPackages() {
        return priorityPackageRepository.findAll();
    }

    public List<SellerPackage> getAllSellerPackages() {
        return sellerPackageRepository.findAll();
    }
}
