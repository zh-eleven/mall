package com.mall.common.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResult<T> {

    private final int code;
    private final String message;
    private final T data;

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(
                ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(),
                data
        );
    }

    public static <T> ApiResult<T> success(T data, String message) {
        return new ApiResult<>(
                ErrorCode.SUCCESS.getCode(),
                message,
                data
        );
    }

    public static <T> ApiResult<T> failed(IErrorCode errorCode) {
        return new ApiResult<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    public static <T> ApiResult<T> failed(
            IErrorCode errorCode,
            String message
    ) {
        return new ApiResult<>(
                errorCode.getCode(),
                message,
                null
        );
    }
}