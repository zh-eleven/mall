package com.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.order.dto.MemberRefundQueryDTO;
import com.mall.order.dto.RefundApplyDTO;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderRefund;
import com.mall.order.enums.OrderStatus;
import com.mall.order.enums.RefundStatus;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.mapper.OmsOrderRefundMapper;
import com.mall.order.service.OrderRefundService;
import com.mall.order.vo.OrderRefundVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderRefundServiceImpl
        implements OrderRefundService {

    private final OmsOrderMapper orderMapper;

    private final OmsOrderRefundMapper refundMapper;

    @Override
    @Transactional
    public OrderRefundVO apply(
            Long memberId,
            Long orderId,
            RefundApplyDTO dto) {

        OmsOrder order = findOwnedOrder(memberId, orderId);

        if (!OrderStatus.PENDING_SHIPMENT.equals(
                order.getStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.REFUND_NOT_ALLOWED
            );
        }

        int updated = orderMapper.markRefunding(
                orderId,
                memberId,
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.REFUNDING.getCode()
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.REFUND_CONCURRENT_OPERATION
            );
        }

        OmsOrderRefund refund = new OmsOrderRefund();
        refund.setRefundSn(generateRefundSn());
        refund.setOrderId(order.getId());
        refund.setOrderSn(order.getOrderSn());
        refund.setMemberId(memberId);
        refund.setRefundAmount(order.getPayAmount());
        refund.setReason(dto.getReason().trim());
        refund.setStatus(RefundStatus.APPLYING);

        try {
            if (refundMapper.insert(refund) != 1) {
                throw new BusinessException(
                        ErrorCode.REFUND_CONCURRENT_OPERATION
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT,
                    exception
            );
        }

        return OrderRefundVO.from(
                findOwnedRefund(memberId, refund.getId())
        );
    }

    @Override
    public PageResult<OrderRefundVO> page(
            Long memberId,
            MemberRefundQueryDTO query) {

        LambdaQueryWrapper<OmsOrderRefund> wrapper =
                new LambdaQueryWrapper<OmsOrderRefund>()
                        .eq(
                                OmsOrderRefund::getMemberId,
                                memberId
                        );

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
    public OrderRefundVO getDetail(
            Long memberId,
            Long refundId) {
        return OrderRefundVO.from(
                findOwnedRefund(memberId, refundId)
        );
    }

    private OmsOrder findOwnedOrder(
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

        return order;
    }

    private OmsOrderRefund findOwnedRefund(
            Long memberId,
            Long refundId) {

        OmsOrderRefund refund = refundMapper.selectOne(
                new LambdaQueryWrapper<OmsOrderRefund>()
                        .eq(OmsOrderRefund::getId, refundId)
                        .eq(
                                OmsOrderRefund::getMemberId,
                                memberId
                        )
        );

        if (refund == null) {
            throw new BusinessException(
                    ErrorCode.REFUND_NOT_FOUND
            );
        }

        return refund;
    }

    private String generateRefundSn() {
        return "R" + UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}
