package com.example.SWP.dto.request.buyer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FeedbackRequest {
    @NotNull(message = "Order ID is required")
    Long orderId;

    @NotNull(message = "Rating is required")
    Integer rating;

    @Max(value = 1000, message = "Comment must be less than 1000 characters")
    String comment;
}
