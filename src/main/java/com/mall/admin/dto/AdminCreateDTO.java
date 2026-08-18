package com.mall.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminCreateDTO {

    @NotBlank(message = "管理员用户名不能为空")
    @Size(min = 4, max = 64)
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9_]*$",
            message = "用户名只能包含字母、数字和下划线"
    )
    private String username;

    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 6, max = 64)
    private String password;

    @Size(max = 100)
    private String nickname;

    @Email(message = "邮箱格式错误")
    @Size(max = 128)
    private String email;

    @Size(max = 255)
    private String avatar;

    @Size(max = 500)
    private String note;

    @Min(0)
    @Max(1)
    private Integer status;
}