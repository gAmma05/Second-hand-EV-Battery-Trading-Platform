package com.example.SWP.exception.handler;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Internal server error: " + ex.getMessage())
                        .data(null)
                        .build());
    }
}
