package com.example.SWP.mapper;

import com.example.SWP.dto.request.buyer.FeedbackRequest;
import com.example.SWP.dto.response.feedback.FeedbackResponse;
import com.example.SWP.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    @Mapping(target = "orderId", source = "order.id")
    Feedback toFeedback(FeedbackRequest request);

    @Mapping(target = "order.id", source = "orderId")
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "id", source = "feedbackId")
    FeedbackResponse toFeedbackResponse(Feedback feedback);

}
