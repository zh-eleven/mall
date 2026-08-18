package com.mall.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminUpdateDTO {

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