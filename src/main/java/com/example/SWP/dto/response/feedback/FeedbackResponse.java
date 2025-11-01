package com.example.SWP.dto.response.feedback;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FeedbackResponse {
    Long feedbackId;
    Long orderId;
    String feedbacker;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
}
