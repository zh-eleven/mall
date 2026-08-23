package com.mall.order.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.impl.OrderCancellationServiceImpl;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCancellationServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                OmsOrder.class,
                OmsOrderItem.class
        );
    }

    @Mock
    private OmsOrderMapper orderMapper;

    @Mock
    private OmsOrderItemMapper orderItemMapper;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @InjectMocks
    private OrderCancellationServiceImpl
            cancellationService;

    @Test
    void memberCancellationShouldReleaseLockedStock() {

        when(orderMapper.selectOne(any()))
                .thenReturn(order(
                        OrderStatus.PENDING_PAYMENT
                ));

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.cancelPendingOrder(
                500L,
                4L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELED.getCode()
        )).thenReturn(1);

        when(skuStockMapper.releaseLockedStock(
                10L,
                2
        )).thenReturn(1);

        cancellationService.cancelByMember(4L, 500L);

        verify(skuStockMapper).releaseLockedStock(
                10L,
                2
        );
    }

    @Test
    void memberCancellationShouldRejectOrderNotOwned() {

        when(orderMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cancellationService
                        .cancelByMember(4L, 500L)
        );

        assertSame(
                ErrorCode.ORDER_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void memberCancellationShouldRejectNonPendingOrder() {

        when(orderMapper.selectOne(any()))
                .thenReturn(order(OrderStatus.SHIPPED));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cancellationService
                        .cancelByMember(4L, 500L)
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );
    }

    @Test
    void memberCancellationShouldRejectConcurrentChange() {

        when(orderMapper.selectOne(any()))
                .thenReturn(order(
                        OrderStatus.PENDING_PAYMENT
                ));

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.cancelPendingOrder(
                500L,
                4L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELED.getCode()
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cancellationService
                        .cancelByMember(4L, 500L)
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );

        verify(skuStockMapper, never())
                .releaseLockedStock(anyLong(), anyInt());
    }

    @Test
    void timedOutPendingOrderShouldCancelAndReleaseStock() {

        LocalDateTime cutoff = LocalDateTime.now()
                .minusMinutes(30);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.cancelTimedOutPendingOrder(
                500L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELED.getCode(),
                cutoff
        )).thenReturn(1);

        when(skuStockMapper.releaseLockedStock(
                10L,
                2
        )).thenReturn(1);

        assertTrue(
                cancellationService.cancelTimedOutOrder(
                        500L,
                        cutoff
                )
        );

        verify(skuStockMapper).releaseLockedStock(
                10L,
                2
        );
    }

    @Test
    void paidOrNonPendingOrderShouldNotReleaseStock() {

        LocalDateTime cutoff = LocalDateTime.now()
                .minusMinutes(30);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.cancelTimedOutPendingOrder(
                500L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELED.getCode(),
                cutoff
        )).thenReturn(0);

        assertFalse(
                cancellationService.cancelTimedOutOrder(
                        500L,
                        cutoff
                )
        );

        verify(skuStockMapper, never())
                .releaseLockedStock(
                        anyLong(),
                        anyInt()
                );
    }

    @Test
    void paymentWinningTimeoutRaceShouldNotReleaseStock() {

        LocalDateTime cutoff = LocalDateTime.now()
                .minusMinutes(30);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.cancelTimedOutPendingOrder(
                500L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELED.getCode(),
                cutoff
        )).thenReturn(0);

        assertFalse(
                cancellationService.cancelTimedOutOrder(
                        500L,
                        cutoff
                )
        );

        verify(skuStockMapper, never())
                .releaseLockedStock(anyLong(), anyInt());
    }

    @Test
    void stockReleaseFailureShouldFailTransaction() {

        LocalDateTime cutoff = LocalDateTime.now()
                .minusMinutes(30);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.cancelTimedOutPendingOrder(
                500L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELED.getCode(),
                cutoff
        )).thenReturn(1);

        when(skuStockMapper.releaseLockedStock(
                10L,
                2
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cancellationService
                        .cancelTimedOutOrder(
                                500L,
                                cutoff
                        )
        );

        assertSame(
                ErrorCode.DATA_CONFLICT,
                exception.getErrorCode()
        );
    }

    private OmsOrder order(OrderStatus status) {
        OmsOrder order = new OmsOrder();
        order.setId(500L);
        order.setMemberId(4L);
        order.setStatus(status);
        return order;
    }

    private OmsOrderItem orderItem() {
        OmsOrderItem item = new OmsOrderItem();
        item.setId(600L);
        item.setOrderId(500L);
        item.setSkuId(10L);
        item.setQuantity(2);
        return item;
    }
}
