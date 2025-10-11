package com.example.SWP.validator.auth;

import com.example.SWP.dto.request.auth.CreateUserRequest;
import com.example.SWP.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class CreateUserRequestValidator{

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";

    public void validateEmail(CreateUserRequest request){
        if(request.getEmail() == null || request.getEmail().isEmpty()){
            throw new BusinessException("Email is required.", 400);
        }

        if(!request.getEmail().matches(EMAIL_REGEX)){
            throw new BusinessException("Email is not valid.", 401);
        }
    }

    public void validatePassword(CreateUserRequest request){

        if(request.getPassword() == null || request.getPassword().isEmpty()){
            throw new BusinessException("Password is required.", 400);
        }

        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new BusinessException("Password and Confirm Password does not match", 400);
        }

        if(!request.getPassword().matches(PASSWORD_REGEX)){
            throw new BusinessException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one special character.", 400);
        }
    }
}
