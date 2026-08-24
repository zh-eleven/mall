package com.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.order.dto.AdminRefundQueryDTO;
import com.mall.order.dto.RefundApproveDTO;
import com.mall.order.dto.RefundRejectDTO;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.entity.OmsOrderRefund;
import com.mall.order.enums.OrderStatus;
import com.mall.order.enums.RefundStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.mapper.OmsOrderRefundMapper;
import com.mall.order.service.AdminOrderRefundService;
import com.mall.order.vo.OrderRefundVO;
import com.mall.product.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderRefundServiceImpl
        implements AdminOrderRefundService {

    private final OmsOrderRefundMapper refundMapper;

    private final OmsOrderMapper orderMapper;

    private final OmsOrderItemMapper orderItemMapper;

    private final PmsSkuStockMapper skuStockMapper;

    @Override
    public PageResult<OrderRefundVO> page(
            AdminRefundQueryDTO query) {

        LambdaQueryWrapper<OmsOrderRefund> wrapper =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getRefundSn())) {
            wrapper.like(
                    OmsOrderRefund::getRefundSn,
                    query.getRefundSn().trim()
            );
        }

        if (StringUtils.hasText(query.getOrderSn())) {
            wrapper.like(
                    OmsOrderRefund::getOrderSn,
                    query.getOrderSn().trim()
            );
        }

        if (query.getMemberId() != null) {
            wrapper.eq(
                    OmsOrderRefund::getMemberId,
                    query.getMemberId()
            );
        }

        if (query.getStatus() != null) {
            wrapper.eq(
                    OmsOrderRefund::getStatus,
                    query.getStatus()
            );
        }

        wrapper.orderByDesc(OmsOrderRefund::getCreateTime)
                .orderByDesc(OmsOrderRefund::getId);

        Page<OmsOrderRefund> page = refundMapper.selectPage(
                new Page<>(
                        query.getPageNum(),
                        query.getPageSize()
                ),
                wrapper
        );

        return PageResult.from(page, OrderRefundVO::from);
    }

    @Override
    public OrderRefundVO getDetail(Long refundId) {
        return OrderRefundVO.from(findRefund(refundId));
    }

    @Override
    @Transactional
    public OrderRefundVO approve(
            Long refundId,
            RefundApproveDTO dto) {

        OmsOrderRefund refund = findApplyingRefund(refundId);

        int orderUpdated = orderMapper.markRefunded(
                refund.getOrderId(),
                OrderStatus.REFUNDING.getCode(),
                OrderStatus.REFUNDED.getCode()
        );

        if (orderUpdated != 1) {
            throw concurrentOperation();
        }

        List<OmsOrderItem> items = findOrderItems(
                refund.getOrderId()
        );

        for (OmsOrderItem item : items) {
            int stockUpdated = skuStockMapper.restoreStock(
                    item.getSkuId(),
                    item.getQuantity()
            );

            if (stockUpdated != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }
        }

        int refundUpdated =
                refundMapper.completeApplyingRefund(
                        refundId,
                        RefundStatus.APPLYING.getCode(),
                        RefundStatus.COMPLETED.getCode(),
                        normalizeText(dto.getAdminNote())
                );

        if (refundUpdated != 1) {
            throw concurrentOperation();
        }

        return getDetail(refundId);
    }

    @Override
    @Transactional
    public OrderRefundVO reject(
            Long refundId,
            RefundRejectDTO dto) {

        OmsOrderRefund refund = findApplyingRefund(refundId);

        int orderUpdated = orderMapper.restorePendingShipment(
                refund.getOrderId(),
                OrderStatus.REFUNDING.getCode(),
                OrderStatus.PENDING_SHIPMENT.getCode()
        );

        if (orderUpdated != 1) {
            throw concurrentOperation();
        }

        int refundUpdated = refundMapper.rejectApplyingRefund(
                refundId,
                RefundStatus.APPLYING.getCode(),
                RefundStatus.REJECTED.getCode(),
                dto.getAdminNote().trim()
        );

        if (refundUpdated != 1) {
            throw concurrentOperation();
        }

        return getDetail(refundId);
    }

    private OmsOrderRefund findApplyingRefund(Long refundId) {
        OmsOrderRefund refund = findRefund(refundId);

        if (!RefundStatus.APPLYING.equals(refund.getStatus())) {
            throw new BusinessException(
                    ErrorCode.REFUND_STATUS_INVALID
            );
        }

        return refund;
    }

    private OmsOrderRefund findRefund(Long refundId) {
        OmsOrderRefund refund = refundMapper.selectById(refundId);

        if (refund == null) {
            throw new BusinessException(
                    ErrorCode.REFUND_NOT_FOUND
            );
        }

        return refund;
    }

    private List<OmsOrderItem> findOrderItems(Long orderId) {
        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>()
                        .eq(OmsOrderItem::getOrderId, orderId)
                        .orderByAsc(OmsOrderItem::getId)
        );

        if (items.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        return items;
    }

    private BusinessException concurrentOperation() {
        return new BusinessException(
                ErrorCode.REFUND_CONCURRENT_OPERATION
        );
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
}
