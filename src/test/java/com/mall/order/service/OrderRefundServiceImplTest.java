package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.order.dto.MemberRefundQueryDTO;
import com.mall.order.dto.RefundApplyDTO;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderRefund;
import com.mall.order.enums.OrderStatus;
import com.mall.order.enums.RefundStatus;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.mapper.OmsOrderRefundMapper;
import com.mall.order.service.impl.OrderRefundServiceImpl;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRefundServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                OmsOrder.class,
                OmsOrderRefund.class
        );
    }

    @Mock
    private OmsOrderMapper orderMapper;

    @Mock
    private OmsOrderRefundMapper refundMapper;

    @Test
    void applyShouldRejectOrderOwnedByAnotherMember() {
        when(orderMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().apply(4L, 500L, applyDTO())
        );

        assertSame(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        verify(orderMapper, never()).markRefunding(
                anyLong(), anyLong(), any(), any()
        );
    }

    @Test
    void applyShouldRejectNonPendingShipmentOrder() {
        when(orderMapper.selectOne(any()))
                .thenReturn(order(OrderStatus.SHIPPED));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().apply(4L, 500L, applyDTO())
        );

        assertSame(
                ErrorCode.REFUND_NOT_ALLOWED,
                exception.getErrorCode()
        );
        verify(refundMapper, never()).insert(
                any(OmsOrderRefund.class)
        );
    }

    @Test
    void applyShouldAtomicallyMarkOrderRefundingAndSaveRefund() {
        OmsOrder order = order(OrderStatus.PENDING_SHIPMENT);
        OmsOrderRefund saved = refund(RefundStatus.APPLYING);

        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.markRefunding(
                500L,
                4L,
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.REFUNDING.getCode()
        )).thenReturn(1);
        when(refundMapper.insert(any(OmsOrderRefund.class)))
                .thenAnswer(invocation -> {
                    OmsOrderRefund entity = invocation.getArgument(0);
                    entity.setId(900L);
                    return 1;
                });
        when(refundMapper.selectOne(any())).thenReturn(saved);

        var result = service().apply(4L, 500L, applyDTO());

        assertEquals(RefundStatus.APPLYING.getCode(), result.status());
        assertEquals(new BigDecimal("198.00"), result.refundAmount());
        verify(orderMapper).markRefunding(
                500L,
                4L,
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.REFUNDING.getCode()
        );
        verify(refundMapper).insert(any(OmsOrderRefund.class));
    }

    @Test
    void applyShouldLoseToConcurrentShippingWithoutSavingRefund() {
        when(orderMapper.selectOne(any()))
                .thenReturn(order(OrderStatus.PENDING_SHIPMENT));
        when(orderMapper.markRefunding(
                500L, 4L, 1, 5
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().apply(4L, 500L, applyDTO())
        );

        assertSame(
                ErrorCode.REFUND_CONCURRENT_OPERATION,
                exception.getErrorCode()
        );
        verify(refundMapper, never()).insert(
                any(OmsOrderRefund.class)
        );
    }

    @Test
    void detailShouldOnlyReturnCurrentMemberRefund() {
        when(refundMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getDetail(4L, 900L)
        );

        assertSame(
                ErrorCode.REFUND_NOT_FOUND,
                exception.getErrorCode()
        );

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Wrapper> captor =
                ArgumentCaptor.forClass(Wrapper.class);
        verify(refundMapper).selectOne(captor.capture());
        AbstractWrapper<?, ?, ?> wrapper =
                (AbstractWrapper<?, ?, ?>) captor.getValue();
        assertTrue(wrapper.getSqlSegment().contains("member_id"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(4L));
    }

    @Test
    void pageShouldFilterByCurrentMemberAndStatus() {
        Page<OmsOrderRefund> data = new Page<>(1, 10);
        data.setTotal(1);
        data.setRecords(List.of(refund(RefundStatus.REJECTED)));
        when(refundMapper.selectPage(any(), any())).thenReturn(data);

        MemberRefundQueryDTO query = new MemberRefundQueryDTO();
        query.setStatus(RefundStatus.REJECTED.getCode());

        var result = service().page(4L, query);

        assertEquals(1, result.total());
        assertEquals(
                RefundStatus.REJECTED.getCode(),
                result.list().getFirst().status()
        );
    }

    private OrderRefundServiceImpl service() {
        return new OrderRefundServiceImpl(
                orderMapper,
                refundMapper
        );
    }

    private RefundApplyDTO applyDTO() {
        RefundApplyDTO dto = new RefundApplyDTO();
        dto.setReason(" 不想要了 ");
        return dto;
    }

    private OmsOrder order(OrderStatus status) {
        OmsOrder order = new OmsOrder();
        order.setId(500L);
        order.setOrderSn("ORDER-500");
        order.setMemberId(4L);
        order.setStatus(status);
        order.setPayAmount(new BigDecimal("198.00"));
        return order;
    }

    private OmsOrderRefund refund(RefundStatus status) {
        OmsOrderRefund refund = new OmsOrderRefund();
        refund.setId(900L);
        refund.setRefundSn("REFUND-900");
        refund.setOrderId(500L);
        refund.setOrderSn("ORDER-500");
        refund.setMemberId(4L);
        refund.setRefundAmount(new BigDecimal("198.00"));
        refund.setReason("不想要了");
        refund.setStatus(status);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        return refund;
    }
}
