package com.mall.seckill.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillActivityCreateDTO {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称不能超过100个字符")
    private String name;

    @NotNull(message = "活动开始时间不能为空")
    @Future(message = "活动开始时间必须晚于当前时间")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    @Future(message = "活动结束时间必须晚于当前时间")
    private LocalDateTime endTime;
}