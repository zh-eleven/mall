package com.mall.common.api;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements IErrorCode {

    SUCCESS(200, "操作成功", HttpStatus.OK),

    PARAM_VALIDATION_FAILED(
            40001,
            "请求参数校验失败",
            HttpStatus.BAD_REQUEST
    ),

    UNAUTHORIZED(
            40100,
            "未登录或登录已过期",
            HttpStatus.UNAUTHORIZED
    ),

    USERNAME_OR_PASSWORD_ERROR(
            40101,
            "用户名或密码错误",
            HttpStatus.UNAUTHORIZED
    ),

    TOKEN_INVALID_OR_EXPIRED(
            40102,
            "Token无效或已过期",
            HttpStatus.UNAUTHORIZED
    ),

    FORBIDDEN(
            40300,
            "没有访问权限",
            HttpStatus.FORBIDDEN
    ),

    MEMBER_DISABLED(
            40301,
            "用户已被禁用",
            HttpStatus.FORBIDDEN
    ),

    MEMBER_NOT_FOUND(
            40401,
            "用户不存在",
            HttpStatus.NOT_FOUND
    ),

    DATA_CONFLICT(
            40900,
            "数据冲突",
            HttpStatus.CONFLICT
    ),

    USERNAME_ALREADY_EXISTS(
            40901,
            "用户名已存在",
            HttpStatus.CONFLICT
    ),

    PHONE_ALREADY_EXISTS(
            40902,
            "手机号已存在",
            HttpStatus.CONFLICT
    ),

    EMAIL_ALREADY_EXISTS(
            40903,
            "邮箱已存在",
            HttpStatus.CONFLICT
    ),

    INTERNAL_SERVER_ERROR(
            50000,
            "服务器内部错误",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}