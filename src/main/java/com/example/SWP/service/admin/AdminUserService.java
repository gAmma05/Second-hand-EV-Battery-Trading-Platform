package com.example.SWP.service.admin;

import com.example.SWP.dto.request.CreateUserRequest;
import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.entity.User;
import com.example.SWP.mapper.UserMapper;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserService {
    UserRepository userRepository;
    UserMapper userMapper;

    public UserResponse blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(false);
        User updated = userRepository.save(user);
        return  userMapper.toUserResponse(updated);
    }

    public UserResponse unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(true);
        User updated = userRepository.save(user);
        return  userMapper.toUserResponse(updated);
    }
}
