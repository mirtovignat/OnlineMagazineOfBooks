package com.example.demo.dto;

import com.example.demo.exception.ErrorCode;
import com.example.demo.exception.SuccessCode;

import java.util.Map;

public record ApiResponse(String message, Map<String, Object> data) {

    public ApiResponse(String message) {
        this(message, Map.of());
    }

    public static ApiResponse success(SuccessCode code, Object... args) {
        return new ApiResponse(code.format(args));
    }

    public static ApiResponse successWithData(SuccessCode code, Map<String, Object> data, Object... args) {
        return new ApiResponse(code.format(args), data);
    }

    public static ApiResponse error(ErrorCode code, Object... args) {
        return new ApiResponse(args.length > 0 ? code.format(args) : code.getMessage());
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(message);
    }
}