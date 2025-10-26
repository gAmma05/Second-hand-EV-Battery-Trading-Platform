package com.example.SWP.validator.seller;

import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class CreateOrderRequestValidator {
    public void validateEmpty(CreateOrderRequest request){
        if(request.getDeliveryMethod() == null){
            throw new BusinessException("Delivery Method is required.", 400);
        }

        if(request.getPaymentType() == null){
            throw new BusinessException("Payment Type is required.", 400);
        }
    }

    public void validateInvalid(CreateOrderRequest request){
        if (!EnumSet.allOf(DeliveryMethod.class).contains(request.getDeliveryMethod())) {
            throw new BusinessException("We don't support that delivery method", 400);
        }

        // PaymentType validation
        if (!EnumSet.allOf(PaymentType.class).contains(request.getPaymentType())) {
            throw new BusinessException("We don't support that payment type", 400);
        }
    }
}
