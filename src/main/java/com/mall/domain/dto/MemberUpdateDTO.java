package com.mall.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberUpdateDTO {

    private String nickname;

    private String phone;

    @Email(message = "邮箱格式错误")
    private String email;

    private String avatar;

    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer gender;

    @PastOrPresent(message = "生日不能晚于当前日期")
    private LocalDate birthday;
}