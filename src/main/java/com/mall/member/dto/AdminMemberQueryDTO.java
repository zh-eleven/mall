package com.mall.member.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AdminMemberQueryDTO {

    private String username;

    private String phone;

    @Min(value = 0, message = "会员状态不能小于0")
    @Max(value = 1, message = "会员状态不能大于1")
    private Integer status;

    @Min(value = 1, message = "页码不能小于1")
    private int pageNum = 1;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private int pageSize = 10;
}
