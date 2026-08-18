package com.mall.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class RoleResourceUpdateDTO {

    @NotNull(message = "资源ID列表不能为空")
    private List<@Positive Long> resourceIds;
}