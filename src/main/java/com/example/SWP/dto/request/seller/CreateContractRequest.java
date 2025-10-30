package com.example.SWP.dto.request.seller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContractRequest {

    @NotNull(message = "orderId is required")
    Long orderId;

    @NotNull(message = "Content is required")
    @Size(max = 1000, message = "Content must be less than 1000 characters")
    String content;
}
