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

    public BigDecimal calculateDepositAmount(BigDecimal fee) {
        return fee.multiply(depositPercentage)
                .setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateRemainingAmount(BigDecimal fee, BigDecimal shippingFee) {
        BigDecimal depositAmount = calculateDepositAmount(fee);
        BigDecimal remaining = fee.subtract(depositAmount).add(shippingFee);
        return remaining.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateShippingFee(Post post, DeliveryMethod deliveryMethod, Integer serviceTypeId, User buyer) {
        BigDecimal shippingFee = BigDecimal.ZERO;

        if (deliveryMethod == DeliveryMethod.GHN) {
            FeeRequest feeRequest = FeeRequest.builder()
                    .serviceTypeId(serviceTypeId)
                    .postId(post.getId())
                    .build();

            FeeResponse feeResponse = ghnService.calculateShippingFee(feeRequest, buyer);
            shippingFee = BigDecimal.valueOf(feeResponse.getTotal());
        }

        return shippingFee;
    }
}
