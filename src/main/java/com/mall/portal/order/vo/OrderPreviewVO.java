package com.mall.portal.order.vo;

import java.math.BigDecimal;
import java.util.List;

public record OrderPreviewVO(
        OrderReceiverVO receiver,
        List<OrderPreviewItemVO> items,
        Integer totalQuantity,
        BigDecimal totalAmount,
        BigDecimal payAmount
) {

    public static OrderPreviewVO from(
            OrderReceiverVO receiver,
            List<OrderPreviewItemVO> items) {

        int totalQuantity = items.stream()
                .mapToInt(OrderPreviewItemVO::quantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(OrderPreviewItemVO::subtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new OrderPreviewVO(
                receiver,
                List.copyOf(items),
                totalQuantity,
                totalAmount,
                totalAmount
        );
    }
}