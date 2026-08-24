package com.mall.order.vo;

import com.mall.order.entity.OmsOrderRefund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderRefundVO(
        Long refundId,
        String refundSn,
        Long orderId,
        String orderSn,
        Long memberId,
        BigDecimal refundAmount,
        String reason,
        Integer status,
        String statusDescription,
        String adminNote,
        LocalDateTime handleTime,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static OrderRefundVO from(OmsOrderRefund refund) {
        return new OrderRefundVO(
                refund.getId(),
                refund.getRefundSn(),
                refund.getOrderId(),
                refund.getOrderSn(),
                refund.getMemberId(),
                refund.getRefundAmount(),
                refund.getReason(),
                refund.getStatus().getCode(),
                refund.getStatus().getDescription(),
                refund.getAdminNote(),
                refund.getHandleTime(),
                refund.getCreateTime(),
                refund.getUpdateTime()
        );
    }
}
