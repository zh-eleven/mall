package com.mall.admin.vo;

import com.mall.admin.entity.UmsAdmin;
import com.mall.admin.entity.UmsResource;

import java.util.List;

public record AdminInfoVO(
        Long adminId,
        String username,
        String nickname,
        String avatar,
        List<String> authorities
) {

    public static AdminInfoVO from(
            UmsAdmin admin,
            List<UmsResource> resources) {

        List<String> authorities = resources.stream()
                .map(UmsResource::getCode)
                .distinct()
                .toList();

        return new AdminInfoVO(
                admin.getId(),
                admin.getUsername(),
                admin.getNickname(),
                admin.getAvatar(),
                authorities
        );
    }
}