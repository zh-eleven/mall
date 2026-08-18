package com.mall.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminLoginDTO {

    @NotBlank(message = "管理员用户名不能为空")
    @Size(max = 64, message = "管理员用户名不能超过64个字符")
    private String username;

    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须为6到64个字符")
    private String password;
}