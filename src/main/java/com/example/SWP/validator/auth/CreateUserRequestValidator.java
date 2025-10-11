package com.example.SWP.validator.auth;

import com.example.SWP.dto.request.auth.CreateUserRequest;
import com.example.SWP.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateUserRequestValidator{

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public void validateEmail(CreateUserRequest request){
        if(request.getEmail() == null || request.getEmail().isEmpty()){
            throw new ValidationException("Email is required.");
        }

        if(!request.getEmail().matches(EMAIL_REGEX)){
            throw new ValidationException("Email is not valid.");
        }
    }

    public void validatePassword(CreateUserRequest request){


        if(request.getPassword() == null || request.getPassword().isEmpty()){
            throw new ValidationException("Password is required.");
        }

        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new ValidationException("Password and Confirm Password does not match");
        }
    }
}
