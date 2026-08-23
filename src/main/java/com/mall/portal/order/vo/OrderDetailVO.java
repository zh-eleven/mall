package com.mall.portal.order.vo;

import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailVO(
        Long orderId,
        String orderSn,
        Integer status,
        String statusDescription,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        OrderDetailReceiverVO receiver,
        String note,
        LocalDateTime createTime,
        LocalDateTime paymentTime,
        LocalDateTime deliveryTime,
        LocalDateTime receiveTime,
        LocalDateTime cancelTime,
        List<OrderDetailItemVO> items
) {

    public static OrderDetailVO from(
            OmsOrder order,
            List<OmsOrderItem> items) {

        List<OrderDetailItemVO> itemVOs =
                items.stream()
                        .map(OrderDetailItemVO::from)
                        .toList();

        return new OrderDetailVO(
                order.getId(),
                order.getOrderSn(),
                order.getStatus().getCode(),
                order.getStatus().getDescription(),
                order.getTotalAmount(),
                order.getPayAmount(),
                OrderDetailReceiverVO.from(order),
                order.getNote(),
                order.getCreateTime(),
                order.getPaymentTime(),
                order.getDeliveryTime(),
                order.getReceiveTime(),
                order.getCancelTime(),
                List.copyOf(itemVOs)
        );
    }
}