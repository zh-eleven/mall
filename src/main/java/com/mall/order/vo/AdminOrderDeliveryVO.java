package com.mall.order.vo;

import com.mall.order.entity.OmsOrder;

import java.time.LocalDateTime;

public record AdminOrderDeliveryVO(
        String deliveryCompany,
        String deliverySn,
        LocalDateTime deliveryTime,
        LocalDateTime receiveTime
) {

    public static AdminOrderDeliveryVO from(
            OmsOrder order) {

        return new AdminOrderDeliveryVO(
                order.getDeliveryCompany(),
                order.getDeliverySn(),
                order.getDeliveryTime(),
                order.getReceiveTime()
        );
    }
}
