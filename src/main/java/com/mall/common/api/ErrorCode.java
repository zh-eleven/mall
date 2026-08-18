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

    ADDRESS_NOT_FOUND(
            40402,
            "收货地址不存在",
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
    ),
    OLD_PASSWORD_ERROR(
            40002,
            "原密码错误",
            HttpStatus.BAD_REQUEST
    ),

    PASSWORD_CONFIRM_NOT_MATCH(
            40003,
            "两次输入的新密码不一致",
            HttpStatus.BAD_REQUEST
    ),
    ADMIN_DISABLED(
            40302,
            "管理员已被禁用",
            HttpStatus.FORBIDDEN
    ),

    ADMIN_NOT_FOUND(
            40403,
            "管理员不存在",
            HttpStatus.NOT_FOUND
    ),
    NEW_PASSWORD_SAME_AS_OLD(
            40004,
            "新密码不能与原密码相同",
            HttpStatus.BAD_REQUEST
    ),
    RESOURCE_NOT_FOUND(
            40404,
            "权限资源不存在",
            HttpStatus.NOT_FOUND
    ),

    RESOURCE_CODE_ALREADY_EXISTS(
            40904,
            "权限编码已存在",
            HttpStatus.CONFLICT
    ),
    RESOURCE_SELECTION_INVALID(
            40005,
            "包含不存在或已禁用的权限资源",
            HttpStatus.BAD_REQUEST
    ),

    ROLE_NOT_FOUND(
            40405,
            "角色不存在",
            HttpStatus.NOT_FOUND
    ),

    ROLE_CODE_ALREADY_EXISTS(
            40905,
            "角色编码已存在",
            HttpStatus.CONFLICT
    ),

    ROLE_PROTECTED(
            40906,
            "超级管理员角色不允许执行该操作",
            HttpStatus.CONFLICT
    ),
    ROLE_SELECTION_INVALID(
            40006,
            "包含不存在或已禁用的角色",
            HttpStatus.BAD_REQUEST
    ),

    ADMIN_ROLE_OPERATION_FORBIDDEN(
            40303,
            "无权执行该角色分配操作",
            HttpStatus.FORBIDDEN
    ),

    ADMIN_USERNAME_ALREADY_EXISTS(
            40907,
            "管理员用户名已存在",
            HttpStatus.CONFLICT
    ),

    ADMIN_EMAIL_ALREADY_EXISTS(
            40908,
            "管理员邮箱已存在",
            HttpStatus.CONFLICT
    ),

    ADMIN_SELF_OPERATION_FORBIDDEN(
            40909,
            "不能删除、禁用或修改自己的角色",
            HttpStatus.CONFLICT
    ),

    SUPER_ADMIN_PROTECTED(
            40910,
            "请先安全移除超级管理员角色",
            HttpStatus.CONFLICT
    ),
    BRAND_NOT_FOUND(
            40406,
            "品牌不存在",
            HttpStatus.NOT_FOUND
    ),
    CATEGORY_NOT_FOUND(
            40407,
            "商品分类不存在",
            HttpStatus.NOT_FOUND
    ),
    CATEGORY_HAS_CHILDREN(
            40915,
            "该分类包含子分类，请先删除子分类",
            HttpStatus.CONFLICT
    ),
    CATEGORY_MOVE_NOT_ALLOWED(
            40914,
            "该分类包含子分类，不能移动到其他分类下",
            HttpStatus.CONFLICT
    ),
    CATEGORY_NAME_ALREADY_EXISTS(
            40913,
            "同级分类名称已存在",
            HttpStatus.CONFLICT
    ),
    CATEGORY_PARENT_INVALID(
            40010,
            "父分类无效，只允许两级分类",
            HttpStatus.BAD_REQUEST
    ),
    LAST_SUPER_ADMIN_PROTECTED(
            40911,
            "系统必须保留至少一位启用的超级管理员",
            HttpStatus.CONFLICT
    ),
    BRAND_NAME_ALREADY_EXISTS(
            40912,
            "品牌名称已存在",
            HttpStatus.CONFLICT
    ),
    BRAND_HAS_PRODUCTS(
            40916,
            "该品牌存在关联商品，无法删除",
            HttpStatus.CONFLICT
    ),
    CATEGORY_HAS_PRODUCTS(
            40917,
            "该分类存在关联商品，无法删除",
            HttpStatus.CONFLICT
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
