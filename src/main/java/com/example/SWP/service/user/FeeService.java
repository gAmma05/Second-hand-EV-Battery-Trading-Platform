package com.example.SWP.service.user;

import com.example.SWP.dto.request.ghn.FeeRequest;
import com.example.SWP.dto.response.ghn.FeeResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.service.ghn.GhnService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FeeService {
    GhnService ghnService;

    @NonFinal
    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;

    /**
     * Tính tổng tiền = fee + phí vận chuyển, làm tròn 0 chữ số thập phân (HALF_UP)
     */
    public BigDecimal calculateTotalAmount(BigDecimal fee, BigDecimal shippingFee) {
        return fee.add(shippingFee)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính tiền đặt cọc = fee * depositPercentage, làm tròn 0 chữ số thập phân (HALF_UP)
     */
    public BigDecimal calculateDepositAmount(BigDecimal fee) {
        return fee.multiply(depositPercentage)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính số tiền còn lại = (fee - tiền đặt cọc) + phí vận chuyển, làm tròn 0 chữ số thập phân (HALF_UP)
     */
    public BigDecimal calculateRemainingAmount(BigDecimal fee, BigDecimal shippingFee) {
        BigDecimal depositAmount = calculateDepositAmount(fee);
        BigDecimal remaining = fee.subtract(depositAmount).add(shippingFee);
        return remaining.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính phí vận chuyển dựa theo phương thức giao hàng và bài đăng
     */
    public BigDecimal calculateShippingFee(Post post, DeliveryMethod deliveryMethod, Integer serviceTypeId, User buyer) {
        // Mặc định phí vận chuyển = 0
        BigDecimal shippingFee = BigDecimal.ZERO;

        // Nếu phương thức giao hàng là GHN thì tiến hành tính phí qua dịch vụ GHN
        if (deliveryMethod == DeliveryMethod.GHN) {
            // Tạo yêu cầu tính phí gửi đến GHN
            FeeRequest feeRequest = FeeRequest.builder()
                    .serviceTypeId(serviceTypeId)
                    .postId(post.getId())
                    .build();

            // Gọi service GHN để lấy phản hồi tính phí vận chuyển
            FeeResponse feeResponse = ghnService.calculateShippingFee(feeRequest, buyer);

            // Gán phí vận chuyển lấy từ phản hồi GHN
            shippingFee = BigDecimal.valueOf(feeResponse.getTotal());
        }

        return shippingFee;
    }
}
