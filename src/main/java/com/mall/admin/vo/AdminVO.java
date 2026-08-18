package com.mall.admin.vo;

import com.mall.admin.entity.UmsAdmin;

import java.time.LocalDateTime;

public record AdminVO(
        Long id,
        String username,
        String nickname,
        String email,
        String avatar,
        String note,
        Integer status,
        LocalDateTime loginTime,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static AdminVO from(UmsAdmin admin) {
        return new AdminVO(
                admin.getId(),
                admin.getUsername(),
                admin.getNickname(),
                admin.getEmail(),
                admin.getAvatar(),
                admin.getNote(),
                admin.getStatus(),
                admin.getLoginTime(),
                admin.getCreateTime(),
                admin.getUpdateTime()
        );
    }
}