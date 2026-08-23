package com.mall.order.vo;

import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderDetailVO(
        Long orderId,
        String orderSn,
        Long memberId,
        Integer status,
        String statusDescription,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        String note,
        AdminOrderReceiverVO receiver,
        AdminOrderDeliveryVO delivery,
        LocalDateTime paymentTime,
        LocalDateTime cancelTime,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        List<AdminOrderItemVO> items
) {

    public static AdminOrderDetailVO from(
            OmsOrder order,
            List<OmsOrderItem> items) {

        return new AdminOrderDetailVO(
                order.getId(),
                order.getOrderSn(),
                order.getMemberId(),
                order.getStatus().getCode(),
                order.getStatus().getDescription(),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getNote(),
                AdminOrderReceiverVO.from(order),
                AdminOrderDeliveryVO.from(order),
                order.getPaymentTime(),
                order.getCancelTime(),
                order.getCreateTime(),
                order.getUpdateTime(),
                items.stream()
                        .map(AdminOrderItemVO::from)
                        .toList()
        );
    }
}
