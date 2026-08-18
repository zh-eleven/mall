package com.mall.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class AdminRoleUpdateDTO {

    @NotNull(message = "角色ID列表不能为空")
    private List<@Positive Long> roleIds;
}