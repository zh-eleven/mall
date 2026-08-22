package com.mall.config;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String[] WHITE_LIST = {
            "/api/members/register",
            "/api/members/login",
            "/api/admin/auth/login",
    };
}