package com.mall.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RoleUpdateDTO {

    @Pattern(
            regexp = ".*\\S.*",
            message = "角色名称不能为空"
    )
    @Size(max = 100)
    private String name;

    @Pattern(
            regexp = "^[A-Z][A-Z0-9_]*$",
            message = "角色编码格式错误"
    )
    private String code;

    @Size(max = 500)
    private String description;

    @Min(0)
    @Max(1)
    private Integer status;

    @Min(0)
    private Integer sort;
}