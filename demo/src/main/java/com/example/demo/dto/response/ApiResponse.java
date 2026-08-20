package com.example.demo.dto.response;

import com.example.demo.exception.SuccessCode;
import lombok.Builder;

import java.util.Map;

@Builder
public record ApiResponse(
        String message,
        Map<String, Object> data
) {
    public ApiResponse(String message) {
        this(message, Map.of());
    }

    public static ApiResponse response(SuccessCode code) {
        return new ApiResponse(code.getMessage());
    }
}