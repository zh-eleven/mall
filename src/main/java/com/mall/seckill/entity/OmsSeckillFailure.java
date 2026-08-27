package com.mall.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oms_seckill_failure")
public class OmsSeckillFailure {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String requestId;

    private Long seckillSkuId;

    private Long memberId;

    private String failureReason;

    /**
     * 0：待补偿
     * 1：已补偿
     */
    private Integer status;

    private Integer retryCount;

    private LocalDateTime lastRetryTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}