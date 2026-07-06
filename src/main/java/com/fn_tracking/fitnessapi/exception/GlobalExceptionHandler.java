package com.fn_tracking.fitnessapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý các lỗi logic mình chủ động ném ra (RuntimeException)
    // Ví dụ: sai tài khoản mật khẩu, trùng email...
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage()); // Sẽ lấy được câu "Username is already taken!"

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // Trả về HTTP 400
    }

    // 2. Xử lý các lỗi validation từ @Valid (khi truyền thiếu data)
    // Ví dụ: Username trống, mật khẩu ngắn hơn 6 ký tự...
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        // Gom tất cả các trường bị lỗi lại
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", fieldErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // Trả về HTTP 400
    }
}