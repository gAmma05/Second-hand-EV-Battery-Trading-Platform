package com.example.SWP.mapper;

import com.example.SWP.dto.response.buyer.PostFavoriteResponse;
import com.example.SWP.entity.PostFavorite;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PostMapper.class})
public interface PostFavoriteMapper {
    PostFavoriteResponse toPostFavoriteResponse(PostFavorite postFavorite);
    List<PostFavoriteResponse> toPostFavoriteResponseList(List<PostFavorite> postFavoriteList);
}

