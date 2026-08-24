package com.mall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.order.enums.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order_refund")
public class OmsOrderRefund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String refundSn;

    private Long orderId;

    private String orderSn;

    private Long memberId;

    private BigDecimal refundAmount;

    private String reason;

    private RefundStatus status;

    private String adminNote;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
