package com.example.jobplatform.exception;

import com.example.jobplatform.common.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage() == null ? "请求参数错误" : error.getDefaultMessage())
            .orElse("请求参数错误");
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return ApiResponse.fail(400, exception.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ApiResponse<Void> handleServiceUnavailable(ServiceUnavailableException exception) {
        return ApiResponse.fail(503, exception.getMessage());
    }

    @ExceptionHandler(DeepseekException.class)
    public ApiResponse<Void> handleDeepseek(DeepseekException exception) {
        return ApiResponse.fail(502, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.fail(500, "系统内部错误");
    }
}

