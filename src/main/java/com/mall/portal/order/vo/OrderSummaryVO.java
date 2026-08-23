package com.mall.portal.order.vo;

import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderSummaryVO(
        Long orderId,
        String orderSn,
        Integer status,
        String statusDescription,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        Integer itemCount,
        Integer totalQuantity,
        String firstProductName,
        String firstProductPic,
        LocalDateTime createTime
) {

    public static OrderSummaryVO from(
            OmsOrder order,
            List<OmsOrderItem> items) {

        int totalQuantity = items.stream()
                .mapToInt(item ->
                        item.getQuantity() == null
                                ? 0
                                : item.getQuantity()
                )
                .sum();

        OmsOrderItem firstItem =
                items.isEmpty()
                        ? null
                        : items.getFirst();

        return new OrderSummaryVO(
                order.getId(),
                order.getOrderSn(),
                order.getStatus().getCode(),
                order.getStatus().getDescription(),
                order.getTotalAmount(),
                order.getPayAmount(),
                items.size(),
                totalQuantity,
                firstItem == null
                        ? null
                        : firstItem.getProductName(),
                firstItem == null
                        ? null
                        : firstItem.getProductPic(),
                order.getCreateTime()
        );
    }
}