package com.example.SWP.mapper;

import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.PostImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "trusted", expression = "java(post.isTrusted())")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    @Mapping(target = "userId", source = "user.id")
    PostResponse toPostResponse(Post post);

    List<PostResponse> toPostResponseList(List<Post> posts);

    @Named("mapImages")
    default List<String> mapImages(List<PostImage> images) {
        if (images == null) return Collections.emptyList();
        return images.stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
