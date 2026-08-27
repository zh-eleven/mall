package com.mall.seckill.vo;

import com.mall.seckill.entity.SmsSeckillActivity;

import java.time.LocalDateTime;

public record SeckillActivityVO(
        Long id,
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static SeckillActivityVO from(
            SmsSeckillActivity activity) {

        return new SeckillActivityVO(
                activity.getId(),
                activity.getName(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getStatus(),
                activity.getCreateTime(),
                activity.getUpdateTime()
        );
    }
}