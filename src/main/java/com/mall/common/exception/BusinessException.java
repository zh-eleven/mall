package com.mall.common.exception;

import com.mall.common.api.IErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final IErrorCode errorCode;

    public BusinessException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(IErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(IErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public BusinessException(
            IErrorCode errorCode,
            String message,
            Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
