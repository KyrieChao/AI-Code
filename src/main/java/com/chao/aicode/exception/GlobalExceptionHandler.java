package com.chao.aicode.exception;

import com.chao.aicode.common.response.ApiResponse;
import com.chao.aicode.common.response.HTTPResponseCode;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.error("BusinessException Error -> {}", ex.getMessage(), ex);
        return ResponseEntity.status(433).body(ApiResponse.error(ex.getStatusCode(), ex.getDescription()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException Error -> {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(ApiResponse.error(HTTPResponseCode.SYSTEM_ERROR, ex.getMessage(), "系统错误"));
    }
}