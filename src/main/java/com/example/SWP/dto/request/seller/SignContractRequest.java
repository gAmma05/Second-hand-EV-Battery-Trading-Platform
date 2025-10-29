package com.example.SWP.dto.request.seller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SignContractRequest {
    @NotNull(message = "Contract ID is required")
    Long contractId;

    @NotNull(message = "Content is required")
    @Size(max = 1000, message = "Content must be less than 1000 characters")
    String content;
}
