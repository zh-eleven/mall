package com.mall.admin.vo;

public record AdminLoginVO(
        Long adminId,
        String username,
        String token
) {
}