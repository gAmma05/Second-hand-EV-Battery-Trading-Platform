package com.example.SWP.dto.request.seller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContractRequest {
    Long orderId;
    String title;
    String content;
    double price;
    String currency;
    boolean sellerSigned;
}
