package com.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.OrderCancellationService;
import com.mall.product.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCancellationServiceImpl
        implements OrderCancellationService {

    private final OmsOrderMapper orderMapper;

    private final OmsOrderItemMapper orderItemMapper;

    private final PmsSkuStockMapper skuStockMapper;

    @Override
    @Transactional
    public void cancelByMember(
            Long memberId,
            Long orderId) {

        OmsOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getId, orderId)
                        .eq(OmsOrder::getMemberId, memberId)
        );

        if (order == null) {
            throw new BusinessException(
                    ErrorCode.ORDER_NOT_FOUND
            );
        }

        if (!OrderStatus.PENDING_PAYMENT.equals(
                order.getStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        List<OmsOrderItem> items =
                findOrderItems(orderId);

        int updated = orderMapper.cancelPendingOrder(
                orderId,
                memberId,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELED.getCode()
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        releaseLockedStocks(items);
    }

    @Override
    @Transactional
    public boolean cancelTimedOutOrder(
            Long orderId,
            LocalDateTime cutoffTime) {

        List<OmsOrderItem> items =
                findOrderItems(orderId);

        int updated =
                orderMapper.cancelTimedOutPendingOrder(
                        orderId,
                        OrderStatus.PENDING_PAYMENT.getCode(),
                        OrderStatus.CANCELED.getCode(),
                        cutoffTime
                );

        if (updated != 1) {
            return false;
        }

        releaseLockedStocks(items);
        return true;
    }

    private List<OmsOrderItem> findOrderItems(
            Long orderId) {

        List<OmsOrderItem> items =
                orderItemMapper.selectList(
                        new LambdaQueryWrapper<OmsOrderItem>()
                                .eq(
                                        OmsOrderItem::getOrderId,
                                        orderId
                                )
                                .orderByAsc(
                                        OmsOrderItem::getId
                                )
                );

        if (items.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        return items;
    }

    private void releaseLockedStocks(
            List<OmsOrderItem> items) {

        for (OmsOrderItem item : items) {
            int updated = skuStockMapper.releaseLockedStock(
                    item.getSkuId(),
                    item.getQuantity()
            );

            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }
        }
    }
}
