package com.chao.oj.common.exception;

import com.chao.oj.common.response.HTTPResponseCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 自定义异常类
 */
@Getter
public class BusinessException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final HTTPResponseCode statusCode;
    private final String description;

    public BusinessException(HTTPResponseCode statusCode) {
        this(statusCode, statusCode.getDescription());
    }

    public BusinessException(HTTPResponseCode statusCode, String description) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
        this.description = description;
    }

    public static BusinessException of(HTTPResponseCode code) {
        return new BusinessException(code);
    }

    public static BusinessException of(HTTPResponseCode code, String description) {
        return new BusinessException(code, description);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public String toString() {
        return "BusinessException{code=" + statusCode.getCode() +
                ", message=" + getMessage() +
                ", description='" + description + '\'' + '}';
    }
}
