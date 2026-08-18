package com.mall.admin.vo;

import com.mall.admin.entity.UmsResource;

import java.time.LocalDateTime;

public record ResourceVO(
        Long id,
        String name,
        String code,
        String urlPattern,
        String httpMethod,
        String description,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static ResourceVO from(UmsResource resource) {
        return new ResourceVO(
                resource.getId(),
                resource.getName(),
                resource.getCode(),
                resource.getUrlPattern(),
                resource.getHttpMethod(),
                resource.getDescription(),
                resource.getStatus(),
                resource.getCreateTime(),
                resource.getUpdateTime()
        );
    }
}