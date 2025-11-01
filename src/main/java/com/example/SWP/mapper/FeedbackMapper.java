package com.example.SWP.mapper;

import com.example.SWP.dto.request.buyer.FeedbackRequest;
import com.example.SWP.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    @Mapping(target = "postId", source = "post.id")
    Feedback toFeedback(FeedbackRequest request);
}
