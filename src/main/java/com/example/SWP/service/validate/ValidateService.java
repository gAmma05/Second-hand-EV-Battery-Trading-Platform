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

    // Validate user hiện tại và trả về User
    public User validateCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));
    }

    public void validatePost(
            ProductType productType, String vehicleBrand, String model, Integer yearOfManufacture,
            String color, Integer mileage, String batteryType, String batteryBrand,
            Integer capacity, String voltage

    ) {
        if(productType == ProductType.VEHICLE) {
            if(vehicleBrand == null || vehicleBrand.isBlank()) {
                throw new BusinessException("Vehicle brand is required for vehicle posts", 400);
            }
            if(model == null || model.isBlank()) {
                throw new BusinessException("Model is required for vehicle posts", 400);
            }
            if(yearOfManufacture == null || yearOfManufacture <= 0
                    || yearOfManufacture > Year.now().getValue()
            ) {
                throw new BusinessException("Year of manufacture is required, must be greater than 0 and cannot be in the future for vehicle posts", 400);
            }
            if(color == null || color.isBlank()) {
                throw new BusinessException("Color is required for vehicle posts", 400);
            }
            if(mileage == null || mileage < 0) {
                throw new BusinessException("Mileage is required and must be non-negative for vehicle posts", 400);
            }
        } else if(productType == ProductType.BATTERY) {
            if(batteryType == null || batteryType.isBlank()) {
                throw new BusinessException("Battery type is required for battery posts", 400);
            }
            if(batteryBrand == null || batteryBrand.isBlank()) {
                throw new BusinessException("Battery brand is required for battery posts", 400);
            }
            if(capacity == null || capacity <= 0) {
                throw new BusinessException("Capacity is required and must be greater than 0 for battery posts", 400);
            }
            if(voltage == null || voltage.isBlank()) {
                throw new BusinessException("Voltage is required for battery posts", 400);
            }
        }
    }
}

