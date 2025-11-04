package com.example.SWP.service.validate;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.request.seller.UpdatePostRequest;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ProductType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class ValidateService {

    private final UserRepository userRepository;

    public User validateCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Người dùng không tồn tại", 404)
        );

        if (!user.isStatus()) {
            throw new BusinessException("Tài khoản hiện tại đang bị khóa", 403);
        }

        return user;
    }

    // Validate dữ liệu bài đăng khi tạo hoặc cập nhật
    public void validatePost(
            ProductType productType, String vehicleBrand, String model, Integer yearOfManufacture,
            String color, Integer mileage, String batteryType, String batteryBrand,
            Integer capacity, String voltage

    ) {
        if (productType == ProductType.VEHICLE) {
            if (vehicleBrand == null || vehicleBrand.isBlank()) {
                throw new BusinessException("Thương hiệu xe là bắt buộc đối với bài đăng về xe", 400);
            }
            if (model == null || model.isBlank()) {
                throw new BusinessException("Mẫu xe là bắt buộc đối với bài đăng về xe", 400);
            }
            if (yearOfManufacture == null || yearOfManufacture <= 0
                    || yearOfManufacture > Year.now().getValue()
            ) {
                throw new BusinessException("Năm sản xuất là bắt buộc, phải lớn hơn 0 và không được vượt quá năm hiện tại đối với bài đăng về xe", 400);
            }
            if (color == null || color.isBlank()) {
                throw new BusinessException("Màu sắc là bắt buộc đối với bài đăng về xe", 400);
            }
            if (mileage == null || mileage < 0) {
                throw new BusinessException("Số km đã đi là bắt buộc và phải lớn hơn hoặc bằng 0 đối với bài đăng về xe", 400);
            }
        } else if (productType == ProductType.BATTERY) {
            if (batteryType == null || batteryType.isBlank()) {
                throw new BusinessException("Loại pin là bắt buộc đối với bài đăng về pin", 400);
            }
            if (batteryBrand == null || batteryBrand.isBlank()) {
                throw new BusinessException("Thương hiệu pin là bắt buộc đối với bài đăng về pin", 400);
            }
            if (capacity == null || capacity <= 0) {
                throw new BusinessException("Dung lượng là bắt buộc và phải lớn hơn 0 đối với bài đăng về pin", 400);
            }
            if (voltage == null || voltage.isBlank()) {
                throw new BusinessException("Hiệu điện thế là bắt buộc đối với bài đăng về pin", 400);
            }
        }
    }

    public void validateAddressInfo(User user) {
        if (user == null) {
            throw new BusinessException("Người dùng không tồn tại", 404);
        }

        if (user.getDistrictId() == null || user.getProvinceId() == null ||
                user.getWardCode() == null || user.getWardCode().isBlank() ||
                user.getStreetAddress() == null || user.getStreetAddress().isBlank()
        ) {
            throw new BusinessException("Thông tin địa chỉ của người dùng chưa đầy đủ. Vui lòng cập nhật địa chỉ trước khi tiếp tục.", 400);
        }
    }


}

