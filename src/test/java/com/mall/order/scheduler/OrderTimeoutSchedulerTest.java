package com.mall.order.scheduler;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.order.config.OrderProperties;
import com.mall.order.enums.OrderStatus;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.OrderCancellationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutSchedulerTest {

    @Mock
    private OmsOrderMapper orderMapper;

    @Mock
    private OrderCancellationService
            orderCancellationService;

    private OrderProperties orderProperties;

    private OrderTimeoutScheduler scheduler;

    @BeforeEach
    void setUp() {
        orderProperties = new OrderProperties();
        orderProperties.setTimeoutMinutes(30);
        orderProperties.setTimeoutBatchSize(100);

        scheduler = new OrderTimeoutScheduler(
                orderMapper,
                orderCancellationService,
                orderProperties
        );
    }

    @Test
    void ordersNotTimedOutShouldNotBeProcessed() {

        when(orderMapper.selectTimedOutPendingOrderIds(
                eq(OrderStatus.PENDING_PAYMENT.getCode()),
                any(LocalDateTime.class),
                eq(100)
        )).thenReturn(List.of());

        scheduler.cancelTimedOutOrders();

        verify(orderCancellationService, never())
                .cancelTimedOutOrder(
                        any(),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void oneFailureShouldNotStopRemainingOrders() {

        when(orderMapper.selectTimedOutPendingOrderIds(
                eq(OrderStatus.PENDING_PAYMENT.getCode()),
                any(LocalDateTime.class),
                eq(100)
        )).thenReturn(List.of(500L, 501L));

        doThrow(new BusinessException(
                ErrorCode.DATA_CONFLICT
        )).when(orderCancellationService)
                .cancelTimedOutOrder(
                        eq(500L),
                        any(LocalDateTime.class)
                );

        scheduler.cancelTimedOutOrders();

        verify(orderCancellationService)
                .cancelTimedOutOrder(
                        eq(501L),
                        any(LocalDateTime.class)
                );
    }
}
