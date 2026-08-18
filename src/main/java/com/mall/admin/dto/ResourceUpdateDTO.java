package com.mall.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResourceUpdateDTO {

    @Pattern(
            regexp = ".*\\S.*",
            message = "资源名称不能为空"
    )
    @Size(max = 100)
    private String name;

    @Pattern(
            regexp = "^[a-z][a-z0-9-]*:[a-z][a-z0-9-]*$",
            message = "权限编码格式错误"
    )
    private String code;

    @Size(max = 255)
    private String urlPattern;

    @Pattern(
            regexp = "^(GET|POST|PUT|PATCH|DELETE|ALL)$",
            message = "HTTP方法格式错误"
    )
    private String httpMethod;

    @Size(max = 500)
    private String description;

    @Min(0)
    @Max(1)
    private Integer status;
}