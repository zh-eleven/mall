package com.mall.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sms_seckill_sku")
public class SmsSeckillSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long productId;

    private Long skuId;

    private BigDecimal seckillPrice;

    /**
     * 活动分配的秒杀总库存。
     */
    private Integer totalStock;

    /**
     * 数据库中剩余的秒杀库存。
     */
    private Integer availableStock;

    private Integer perUserLimit;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}