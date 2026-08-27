package com.mall.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oms_seckill_order")
public class OmsSeckillOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String requestId;

    private Long orderId;

    private Long activityId;

    private Long seckillSkuId;

    private Long memberId;

    private Integer quantity;

    private LocalDateTime createTime;
}