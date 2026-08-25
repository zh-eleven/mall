package com.mall.order.mq;

import com.mall.order.config.OrderProperties;
import com.mall.order.service.OrderCancellationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutMessageListenerTest {

    @Mock
    private OrderCancellationService
            orderCancellationService;

    @Mock
    private OrderProperties orderProperties;

    @InjectMocks
    private OrderTimeoutMessageListener listener;

    @Test
    void shouldCancelTimedOutOrder() {

        when(orderProperties.getTimeoutMinutes())
                .thenReturn(30L);

        when(orderCancellationService.cancelTimedOutOrder(
                eq(10L),
                any(LocalDateTime.class)
        )).thenReturn(true);

        LocalDateTime before =
                LocalDateTime.now().minusMinutes(30);

        listener.handle(
                new OrderTimeoutMessage(
                        10L,
                        LocalDateTime.now()
                )
        );

        LocalDateTime after =
                LocalDateTime.now().minusMinutes(30);

        ArgumentCaptor<LocalDateTime> captor =
                ArgumentCaptor.forClass(
                        LocalDateTime.class
                );

        verify(orderCancellationService)
                .cancelTimedOutOrder(
                        eq(10L),
                        captor.capture()
                );

        LocalDateTime cutoffTime = captor.getValue();

        assertFalse(cutoffTime.isBefore(before));
        assertFalse(cutoffTime.isAfter(after));
    }

    @Test
    void shouldIgnoreAlreadyProcessedOrder() {

        when(orderProperties.getTimeoutMinutes())
                .thenReturn(30L);

        when(orderCancellationService.cancelTimedOutOrder(
                eq(20L),
                any(LocalDateTime.class)
        )).thenReturn(false);

        assertDoesNotThrow(() ->
                listener.handle(
                        new OrderTimeoutMessage(
                                20L,
                                LocalDateTime.now()
                        )
                )
        );

        verify(orderCancellationService)
                .cancelTimedOutOrder(
                        eq(20L),
                        any(LocalDateTime.class)
                );
    }
}