package com.mall.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_seckill_activity")
public class SmsSeckillActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 0：未启用
     * 1：已启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}