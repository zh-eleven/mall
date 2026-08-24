package com.mall.member.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberStatusUpdateDTO {

    @NotNull(message = "会员状态不能为空")
    @Min(value = 0, message = "会员状态不能小于0")
    @Max(value = 1, message = "会员状态不能大于1")
    private Integer status;
}
