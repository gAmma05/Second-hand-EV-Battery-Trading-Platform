package com.example.SWP.service.validate;

import com.example.SWP.entity.User;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateService {

    private final UserRepository userRepository;

    // Validate user hiện tại và trả về User
    public User validateCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));
    }
}

