package com.example.SWP.dto.request.seller;

import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContractRequest {

    @NotNull(message = "orderId is required")
    Long orderId;

    @NotNull(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    String title;

    @NotNull(message = "Content is required")
    @Size(max = 1000, message = "Content must be less than 1000 characters")
    String content;

    @NotNull(message = "Price is required")
    double price;

    @NotNull(message = "Currency is required")
    String currency;

    @NotNull(message = "SellerSigned is required")
    boolean sellerSigned;

    @NotNull(message = "PaymentType is required")
    PaymentType paymentType;
}
