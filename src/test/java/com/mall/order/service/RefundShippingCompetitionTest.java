package com.mall.order.service;

import com.mall.order.dto.OrderShipDTO;
import com.mall.order.dto.RefundApplyDTO;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderRefund;
import com.mall.order.enums.OrderStatus;
import com.mall.order.enums.RefundStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.mapper.OmsOrderRefundMapper;
import com.mall.order.service.impl.AdminOrderServiceImpl;
import com.mall.order.service.impl.OrderRefundServiceImpl;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefundShippingCompetitionTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                OmsOrder.class,
                OmsOrderRefund.class
        );
    }

    @Test
    void refundApplicationAndShippingShouldHaveOnlyOneWinner()
            throws Exception {

        OmsOrderMapper orderMapper = mock(OmsOrderMapper.class);
        OmsOrderItemMapper itemMapper = mock(OmsOrderItemMapper.class);
        OmsOrderRefundMapper refundMapper =
                mock(OmsOrderRefundMapper.class);

        AtomicReference<OrderStatus> databaseStatus =
                new AtomicReference<>(OrderStatus.PENDING_SHIPMENT);
        CountDownLatch bothReadPending = new CountDownLatch(2);

        when(orderMapper.selectOne(any())).thenAnswer(invocation -> {
            bothReadPending.countDown();
            assertTrue(bothReadPending.await(2, TimeUnit.SECONDS));
            return order(OrderStatus.PENDING_SHIPMENT);
        });

        when(orderMapper.selectById(500L)).thenAnswer(invocation -> {
            if (databaseStatus.get() == OrderStatus.PENDING_SHIPMENT) {
                bothReadPending.countDown();
                assertTrue(bothReadPending.await(2, TimeUnit.SECONDS));
                return order(OrderStatus.PENDING_SHIPMENT);
            }
            return order(databaseStatus.get());
        });

        when(orderMapper.markRefunding(500L, 4L, 1, 5))
                .thenAnswer(invocation ->
                        databaseStatus.compareAndSet(
                                OrderStatus.PENDING_SHIPMENT,
                                OrderStatus.REFUNDING
                        ) ? 1 : 0
                );

        when(orderMapper.shipPendingOrder(
                500L, "顺丰速运", "SF500", 1, 2
        )).thenAnswer(invocation ->
                databaseStatus.compareAndSet(
                        OrderStatus.PENDING_SHIPMENT,
                        OrderStatus.SHIPPED
                ) ? 1 : 0
        );

        when(refundMapper.insert(any(OmsOrderRefund.class)))
                .thenAnswer(invocation -> {
            OmsOrderRefund refund = invocation.getArgument(0);
            refund.setId(900L);
            return 1;
                });
        when(refundMapper.selectOne(any()))
                .thenReturn(refund());
        when(itemMapper.selectList(any()))
                .thenReturn(java.util.List.of());

        OrderRefundServiceImpl refundService =
                new OrderRefundServiceImpl(orderMapper, refundMapper);
        AdminOrderServiceImpl adminOrderService =
                new AdminOrderServiceImpl(orderMapper, itemMapper);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var refundFuture = executor.submit(() -> succeeds(() ->
                    refundService.apply(4L, 500L, applyDTO())
            ));
            var shippingFuture = executor.submit(() -> succeeds(() ->
                    adminOrderService.ship(500L, shipDTO())
            ));

            boolean refundSucceeded = refundFuture.get();
            boolean shippingSucceeded = shippingFuture.get();

            assertEquals(1,
                    (refundSucceeded ? 1 : 0)
                            + (shippingSucceeded ? 1 : 0));
            assertTrue(databaseStatus.get() == OrderStatus.REFUNDING
                    || databaseStatus.get() == OrderStatus.SHIPPED);
        }
    }

    private boolean succeeds(Runnable operation) {
        try {
            operation.run();
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static OmsOrder order(OrderStatus status) {
        OmsOrder order = new OmsOrder();
        order.setId(500L);
        order.setOrderSn("ORDER-500");
        order.setMemberId(4L);
        order.setStatus(status);
        order.setPayAmount(new BigDecimal("198.00"));
        return order;
    }

    private OmsOrderRefund refund() {
        OmsOrderRefund refund = new OmsOrderRefund();
        refund.setId(900L);
        refund.setRefundSn("REFUND-900");
        refund.setOrderId(500L);
        refund.setOrderSn("ORDER-500");
        refund.setMemberId(4L);
        refund.setRefundAmount(new BigDecimal("198.00"));
        refund.setReason("不想要了");
        refund.setStatus(RefundStatus.APPLYING);
        return refund;
    }

    private RefundApplyDTO applyDTO() {
        RefundApplyDTO dto = new RefundApplyDTO();
        dto.setReason("不想要了");
        return dto;
    }

    private OrderShipDTO shipDTO() {
        OrderShipDTO dto = new OrderShipDTO();
        dto.setDeliveryCompany("顺丰速运");
        dto.setDeliverySn("SF500");
        return dto;
    }
}
