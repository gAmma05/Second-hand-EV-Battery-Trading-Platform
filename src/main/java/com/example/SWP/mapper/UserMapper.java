package com.example.SWP.mapper;

import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    List<UserResponse> toUserResponseList(List<User> users);
}
