package com.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.order.dto.AdminOrderQueryDTO;
import com.mall.order.dto.OrderShipDTO;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.AdminOrderService;
import com.mall.order.vo.AdminOrderDetailVO;
import com.mall.order.vo.AdminOrderSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderServiceImpl
        implements AdminOrderService {

    private final OmsOrderMapper orderMapper;

    private final OmsOrderItemMapper orderItemMapper;

    @Override
    public PageResult<AdminOrderSummaryVO> page(
            AdminOrderQueryDTO query) {

        LambdaQueryWrapper<OmsOrder> wrapper =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getOrderSn())) {
            wrapper.like(
                    OmsOrder::getOrderSn,
                    query.getOrderSn().trim()
            );
        }

        if (query.getMemberId() != null) {
            wrapper.eq(
                    OmsOrder::getMemberId,
                    query.getMemberId()
            );
        }

        if (query.getStatus() != null) {
            wrapper.eq(
                    OmsOrder::getStatus,
                    query.getStatus()
            );
        }

        wrapper.orderByDesc(OmsOrder::getCreateTime)
                .orderByDesc(OmsOrder::getId);

        Page<OmsOrder> page = orderMapper.selectPage(
                new Page<>(
                        query.getPageNum(),
                        query.getPageSize()
                ),
                wrapper
        );

        Map<Long, List<OmsOrderItem>> itemsByOrder =
                findItemsByOrder(page.getRecords());

        return PageResult.from(
                page,
                order -> AdminOrderSummaryVO.from(
                        order,
                        itemsByOrder.getOrDefault(
                                order.getId(),
                                List.of()
                        )
                )
        );
    }

    @Override
    public AdminOrderDetailVO getDetail(Long orderId) {

        OmsOrder order = findOrder(orderId);

        return AdminOrderDetailVO.from(
                order,
                findOrderItems(orderId)
        );
    }

    @Override
    @Transactional
    public AdminOrderDetailVO ship(
            Long orderId,
            OrderShipDTO dto) {

        OmsOrder order = findOrder(orderId);

        if (!OrderStatus.PENDING_SHIPMENT.equals(
                order.getStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        int updated = orderMapper.shipPendingOrder(
                orderId,
                dto.getDeliveryCompany().trim(),
                dto.getDeliverySn().trim(),
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.SHIPPED.getCode()
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        return getDetail(orderId);
    }

    private OmsOrder findOrder(Long orderId) {

        OmsOrder order = orderMapper.selectById(orderId);

        if (order == null) {
            throw new BusinessException(
                    ErrorCode.ORDER_NOT_FOUND
            );
        }

        return order;
    }

    private List<OmsOrderItem> findOrderItems(
            Long orderId) {

        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>()
                        .eq(
                                OmsOrderItem::getOrderId,
                                orderId
                        )
                        .orderByAsc(
                                OmsOrderItem::getId
                        )
        );
    }

    private Map<Long, List<OmsOrderItem>>
    findItemsByOrder(List<OmsOrder> orders) {

        if (orders.isEmpty()) {
            return Map.of();
        }

        List<Long> orderIds = orders.stream()
                .map(OmsOrder::getId)
                .toList();

        return orderItemMapper.selectList(
                        new LambdaQueryWrapper<OmsOrderItem>()
                                .in(
                                        OmsOrderItem::getOrderId,
                                        orderIds
                                )
                                .orderByAsc(
                                        OmsOrderItem::getId
                                )
                )
                .stream()
                .collect(Collectors.groupingBy(
                        OmsOrderItem::getOrderId
                ));
    }
}
