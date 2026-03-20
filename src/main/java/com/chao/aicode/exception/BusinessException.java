package com.chao.aicode.exception;

import com.chao.aicode.common.response.HTTPResponseCode;
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
    private final String method;// null 表示不打印

    public BusinessException(HTTPResponseCode statusCode) {
        this(statusCode, statusCode.getDescription(), null);
    }

    public BusinessException(HTTPResponseCode statusCode, String description) {
        this(statusCode, description, null);
    }

    private BusinessException(HTTPResponseCode statusCode, String description, String method) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
        this.description = description;
        this.method = method;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HTTPResponseCode statusCode;
        private String description;
        private String method;

        public Builder code(HTTPResponseCode code) {
            this.statusCode = code;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public BusinessException build() {
            if (statusCode == null) throw new IllegalArgumentException("statusCode 不能为空");
            if (description == null) description = statusCode.getDescription();
            return new BusinessException(statusCode, description, method);
        }
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64)
                .append("BusinessException{code=").append(statusCode.getCode())
                .append(", message=").append(statusCode.getMessage())
                .append(", description='").append(description).append('\'');
        if (method != null) {          // 多判一次 null
            sb.append(", method='").append(method).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
