package com.mall.portal.order.vo;

import com.mall.order.entity.OmsOrder;

import java.math.BigDecimal;

public record OrderSubmitVO(
        Long orderId,
        String orderSn,
        Integer status,
        String statusDescription,
        BigDecimal payAmount
) {

    public static OrderSubmitVO from(
            OmsOrder order) {

        return new OrderSubmitVO(
                order.getId(),
                order.getOrderSn(),
                order.getStatus().getCode(),
                order.getStatus().getDescription(),
                order.getPayAmount()
        );
    }
}