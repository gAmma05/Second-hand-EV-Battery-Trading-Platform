package com.example.SWP.mapper;

import com.example.SWP.dto.response.UserProfileResponse;
import com.example.SWP.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserProfileResponse toUserProfileResponse(User user);
}
