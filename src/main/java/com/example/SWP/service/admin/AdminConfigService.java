package com.example.SWP.service.admin;

import com.example.SWP.dto.request.admin.AppConfigRequest;
import com.example.SWP.dto.request.admin.PriorityPackageRequest;
import com.example.SWP.dto.request.admin.SellerPackageRequest;
import com.example.SWP.entity.AppConfig;
import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.exception.BusinessException; // Giả định bạn có lớp này
import com.example.SWP.repository.AppConfigRepository;
import com.example.SWP.repository.PriorityPackageRepository;
import com.example.SWP.repository.SellerPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminConfigService {

    PriorityPackageRepository priorityPackageRepository;
    SellerPackageRepository sellerPackageRepository;
    AppConfigRepository appConfigRepository;

    /**
     * Lấy tất cả các gói bán hàng
     */
    public List<SellerPackage> findAllSellerPackages() {
        return sellerPackageRepository.findAll();
    }

    /**
     * Tạo gói bán hàng mới
     */
    public SellerPackage createSellerPackage(SellerPackageRequest request) {
        SellerPackage newPackage = SellerPackage.builder()
                .description(request.getDescription())
                .price(request.getPrice())
                .postLimit(request.getPostLimit())
                .build();
        return sellerPackageRepository.save(newPackage);
    }

    /**
     * Cập nhật thông tin gói bán hàng
     */
    public SellerPackage updateSellerPackage(Long id, SellerPackageRequest request) {
        SellerPackage existingPackage = sellerPackageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Gói bán hàng không tồn tại", 404));

        // Cập nhật các trường dữ liệu
        existingPackage.setDescription(request.getDescription());
        existingPackage.setPrice(request.getPrice());
        existingPackage.setPostLimit(request.getPostLimit());

        return sellerPackageRepository.save(existingPackage);
    }

    /**
     * Xóa gói bán hàng
     */
    public void deleteSellerPackage(Long id) {
        if (!sellerPackageRepository.existsById(id)) {
            throw new BusinessException("Gói bán hàng không tồn tại", 404);
        }
        sellerPackageRepository.deleteById(id);
    }

    /**
     * Lấy tất cả các gói ưu tiên
     */
    public List<PriorityPackage> findAllPriorityPackages() {
        return priorityPackageRepository.findAll();
    }

    /**
     * Tạo gói ưu tiên mới
     */
    public PriorityPackage createPriorityPackage(PriorityPackageRequest request) {
        PriorityPackage newPackage = PriorityPackage.builder()
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .build();
        return priorityPackageRepository.save(newPackage);
    }

    /**
     * Cập nhật thông tin gói ưu tiên
     */
    public PriorityPackage updatePriorityPackage(Long id, PriorityPackageRequest request) {
        PriorityPackage existingPackage = priorityPackageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Gói ưu tiên không tồn tại", 404));

        // Cập nhật các trường dữ liệu
        existingPackage.setDescription(request.getDescription());
        existingPackage.setPrice(request.getPrice());
        existingPackage.setDurationDays(request.getDurationDays());

        return priorityPackageRepository.save(existingPackage);
    }

    /**
     * Xóa gói ưu tiên
     */
    public void deletePriorityPackage(Long id) {
        if (!priorityPackageRepository.existsById(id)) {
            throw new BusinessException("Gói ưu tiên không tồn tại", 404);
        }
        priorityPackageRepository.deleteById(id);
    }

    /**
     * Lấy tất cả các cấu hình hệ thống
     */
    public List<AppConfig> findAllAppConfigs() {
        return appConfigRepository.findAll();
    }

    /**
     * Thêm cấu hình hệ thống
     */
    @Transactional
    public AppConfig createAppConfig(AppConfigRequest request) {
        String key = request.getConfigKey().toUpperCase();

        if (appConfigRepository.existsByConfigKey(key)) {
            throw new BusinessException("Key cấu hình này đã tồn tại trong hệ thống: ", 400);
        }

        AppConfig newConfig = AppConfig.builder()
                .configKey(key)
                .configValue(request.getConfigValue())
                .description(request.getDescription())
                .build();

        return appConfigRepository.save(newConfig);
    }

    /**
     * Cập nhật cấu hình hệ thống
     */
    @Transactional
    public AppConfig updateAppConfig(AppConfigRequest request) {
        AppConfig existingConfig = appConfigRepository.findByConfigKey(request.getConfigKey())
                .orElseThrow(() -> new BusinessException("Cấu hình không tồn tại trong hệ thống", 404));

        existingConfig.setConfigValue(request.getConfigValue());
        existingConfig.setDescription(request.getDescription());

        return appConfigRepository.save(existingConfig);
    }

    /**
     * Xóa cấu hình hệ thống bằng ID
     */
    @Transactional
    public void deleteAppConfigById(Long id) {
        // Kiểm tra cấu hình có tồn tại không
        if (!appConfigRepository.existsById(id)) {
            throw new BusinessException("ID cấu hình này không tồn tại trong hệ thống", 404);
        }

        appConfigRepository.deleteById(id);
    }

    /**
     * Lấy cấu hình của đặt cọc
     */
    public BigDecimal getDepositPercentage() {
        // Kiểm tra câ hình
        AppConfig config = appConfigRepository.findByConfigKey("DEPOSIT_PERCENTAGE_KEY")
                .orElseThrow(() -> new BusinessException("Cấu hình tỉ lệ đặt cọc bị thiếu. Vui lòng liên hệ Admin.", 500));

        // Chuyển đổi từ String sang BigDecimal
        try {
            // Đặt Scale cho độ chính xác
            return new BigDecimal(config.getConfigValue()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new BusinessException("Giá trị cấu hình tỉ lệ đặt cọc không hợp lệ. Vui lòng liên hệ Admin.", 500);
        }
    }
}