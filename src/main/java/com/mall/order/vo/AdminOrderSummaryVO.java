package com.mall.order.vo;

import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderSummaryVO(
        Long orderId,
        String orderSn,
        Long memberId,
        Integer status,
        String statusDescription,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        String receiverName,
        String receiverPhone,
        Integer itemCount,
        Integer totalQuantity,
        String firstProductName,
        String firstProductPic,
        LocalDateTime createTime
) {

    public static AdminOrderSummaryVO from(
            OmsOrder order,
            List<OmsOrderItem> items) {

        int totalQuantity = items.stream()
                .mapToInt(item ->
                        item.getQuantity() == null
                                ? 0
                                : item.getQuantity()
                )
                .sum();

        OmsOrderItem firstItem = items.isEmpty()
                ? null
                : items.getFirst();

        return new AdminOrderSummaryVO(
                order.getId(),
                order.getOrderSn(),
                order.getMemberId(),
                order.getStatus().getCode(),
                order.getStatus().getDescription(),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getReceiverName(),
                order.getReceiverPhone(),
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
