package com.mall.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundApproveDTO {

    @Size(max = 500, message = "管理员备注长度不能超过500个字符")
    private String adminNote;
}
