package com.example.SWP.mapper;

import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.Post;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {
    List<PostResponse> toPostResponseList(List<Post> posts);
}
