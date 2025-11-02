package com.example.SWP.mapper;

import com.example.SWP.dto.request.buyer.FeedbackRequest;
import com.example.SWP.dto.response.feedback.FeedbackResponse;
import com.example.SWP.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    @Mapping(source = "orderId", target = "order.id")
    Feedback toFeedback(FeedbackRequest request);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "id", target = "feedbackId")
    FeedbackResponse toFeedbackResponse(Feedback feedback);

}
