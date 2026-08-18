package com.mall.security;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String[] WHITE_LIST = {
            "/api/members/register",
            "/api/members/login"
    };
}