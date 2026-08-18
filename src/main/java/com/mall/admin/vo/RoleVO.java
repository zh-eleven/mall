package com.mall.admin.vo;

import com.mall.admin.entity.UmsRole;

import java.time.LocalDateTime;

public record RoleVO(
        Long id,
        String name,
        String code,
        String description,
        Integer status,
        Integer sort,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static RoleVO from(UmsRole role) {
        return new RoleVO(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                role.getStatus(),
                role.getSort(),
                role.getCreateTime(),
                role.getUpdateTime()
        );
    }
}