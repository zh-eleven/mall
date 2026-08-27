package com.mall.order.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.order.dto.RefundApproveDTO;
import com.mall.order.dto.RefundRejectDTO;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.entity.OmsOrderRefund;
import com.mall.order.enums.OrderStatus;
import com.mall.order.enums.RefundStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.mapper.OmsOrderRefundMapper;
import com.mall.order.service.impl.AdminOrderRefundServiceImpl;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.mall.product.mapper.PmsProductMapper;

@ExtendWith(MockitoExtension.class)
class AdminOrderRefundServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                OmsOrderRefund.class,
                OmsOrderItem.class
        );
    }

    @Mock
    private OmsOrderRefundMapper refundMapper;

    @Mock
    private OmsOrderMapper orderMapper;

    @Mock
    private OmsOrderItemMapper orderItemMapper;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @Mock
    private PmsProductMapper productMapper;

    @Test
    void approveShouldMarkOrderRefundedRestoreStockAndCompleteRefund() {
        OmsOrderRefund applying = refund(RefundStatus.APPLYING);
        OmsOrderRefund completed = refund(RefundStatus.COMPLETED);

        when(refundMapper.selectById(900L))
                .thenReturn(applying, completed);
        when(orderMapper.markRefunded(500L, 5, 6))
                .thenReturn(1);
        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(item(10L, 2), item(11L, 1)));
        when(skuStockMapper.restoreStock(10L, 2)).thenReturn(1);
        when(skuStockMapper.restoreStock(11L, 1)).thenReturn(1);
        when(productMapper.increaseStock(20L, 3)).thenReturn(1);
        when(refundMapper.completeApplyingRefund(
                900L, 0, 1, "审核通过"
        )).thenReturn(1);

        var result = service().approve(900L, approveDTO());

        assertEquals(OrderStatus.REFUNDED.getCode(), 6);
        assertEquals(RefundStatus.COMPLETED.getCode(), result.status());
        verify(orderMapper).markRefunded(500L, 5, 6);
        verify(skuStockMapper).restoreStock(10L, 2);
        verify(skuStockMapper).restoreStock(11L, 1);
        verify(productMapper).increaseStock(20L, 3);
        verify(refundMapper).completeApplyingRefund(
                900L, 0, 1, "审核通过"
        );
    }

    @Test
    void repeatedApprovalShouldNotRestoreStockAgain() {
        when(refundMapper.selectById(900L))
                .thenReturn(refund(RefundStatus.COMPLETED));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().approve(900L, approveDTO())
        );

        assertSame(
                ErrorCode.REFUND_STATUS_INVALID,
                exception.getErrorCode()
        );
        verify(orderMapper, never()).markRefunded(
                anyLong(), any(), any()
        );
        verify(skuStockMapper, never()).restoreStock(
                anyLong(), any()
        );
    }

    @Test
    void concurrentApprovalShouldFailBeforeRestoringStock() {
        when(refundMapper.selectById(900L))
                .thenReturn(refund(RefundStatus.APPLYING));
        when(orderMapper.markRefunded(500L, 5, 6))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().approve(900L, approveDTO())
        );

        assertSame(
                ErrorCode.REFUND_CONCURRENT_OPERATION,
                exception.getErrorCode()
        );
        verify(skuStockMapper, never()).restoreStock(
                anyLong(), any()
        );
    }

    @Test
    void twoConcurrentApprovalsShouldRestoreStockOnlyOnce()
            throws Exception {
        AdminOrderRefundServiceImpl service = service();
        CountDownLatch bothLoadedApplying = new CountDownLatch(2);
        AtomicInteger refundReads = new AtomicInteger();
        AtomicReference<OrderStatus> orderStatus =
                new AtomicReference<>(OrderStatus.REFUNDING);

        when(refundMapper.selectById(900L)).thenAnswer(invocation -> {
            if (refundReads.incrementAndGet() <= 2) {
                bothLoadedApplying.countDown();
                org.junit.jupiter.api.Assertions.assertTrue(
                        bothLoadedApplying.await(2, TimeUnit.SECONDS)
                );
                return refund(RefundStatus.APPLYING);
            }
            return refund(RefundStatus.COMPLETED);
        });
        when(orderMapper.markRefunded(500L, 5, 6))
                .thenAnswer(invocation ->
                        orderStatus.compareAndSet(
                                OrderStatus.REFUNDING,
                                OrderStatus.REFUNDED
                        ) ? 1 : 0
                );
        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(item(10L, 2)));
        when(skuStockMapper.restoreStock(10L, 2)).thenReturn(1);
        when(productMapper.increaseStock(20L, 2)).thenReturn(1);
        when(refundMapper.completeApplyingRefund(
                900L, 0, 1, "审核通过"
        )).thenReturn(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> approveSucceeds(service));
            var second = executor.submit(() -> approveSucceeds(service));

            assertEquals(1,
                    (first.get() ? 1 : 0)
                            + (second.get() ? 1 : 0));
        }

        verify(skuStockMapper, times(1)).restoreStock(10L, 2);
        verify(productMapper, times(1)).increaseStock(20L, 2);
        verify(refundMapper, times(1)).completeApplyingRefund(
                900L, 0, 1, "审核通过"
        );
    }

    @Test
    void approveShouldCheckEveryStockUpdate() {
        when(refundMapper.selectById(900L))
                .thenReturn(refund(RefundStatus.APPLYING));
        when(orderMapper.markRefunded(500L, 5, 6))
                .thenReturn(1);
        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(item(10L, 2)));
        when(skuStockMapper.restoreStock(10L, 2))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().approve(900L, approveDTO())
        );

        assertSame(ErrorCode.DATA_CONFLICT, exception.getErrorCode());
        verify(refundMapper, never()).completeApplyingRefund(
                anyLong(), any(), any(), any()
        );
    }

    @Test
    void approveShouldCheckFinalRefundAtomicUpdate() {
        when(refundMapper.selectById(900L))
                .thenReturn(refund(RefundStatus.APPLYING));
        when(orderMapper.markRefunded(500L, 5, 6))
                .thenReturn(1);
        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(item(10L, 2)));
        when(skuStockMapper.restoreStock(10L, 2))
                .thenReturn(1);
        when(productMapper.increaseStock(20L, 2))
                .thenReturn(1);
        when(refundMapper.completeApplyingRefund(
                900L, 0, 1, "审核通过"
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().approve(900L, approveDTO())
        );

        assertSame(
                ErrorCode.REFUND_CONCURRENT_OPERATION,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectShouldRestoreOrderToPendingShipment() {
        OmsOrderRefund applying = refund(RefundStatus.APPLYING);
        OmsOrderRefund rejected = refund(RefundStatus.REJECTED);

        when(refundMapper.selectById(900L))
                .thenReturn(applying, rejected);
        when(orderMapper.restorePendingShipment(500L, 5, 1))
                .thenReturn(1);
        when(refundMapper.rejectApplyingRefund(
                900L, 0, 2, "不符合退款条件"
        )).thenReturn(1);

        var result = service().reject(900L, rejectDTO());

        assertEquals(RefundStatus.REJECTED.getCode(), result.status());
        verify(orderMapper).restorePendingShipment(500L, 5, 1);
        verify(refundMapper).rejectApplyingRefund(
                900L, 0, 2, "不符合退款条件"
        );
        verify(skuStockMapper, never()).restoreStock(
                anyLong(), any()
        );
    }

    @Test
    void rejectShouldFailWhenOrderWasConcurrentlyChanged() {
        when(refundMapper.selectById(900L))
                .thenReturn(refund(RefundStatus.APPLYING));
        when(orderMapper.restorePendingShipment(500L, 5, 1))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().reject(900L, rejectDTO())
        );

        assertSame(
                ErrorCode.REFUND_CONCURRENT_OPERATION,
                exception.getErrorCode()
        );
        verify(refundMapper, never()).rejectApplyingRefund(
                anyLong(), any(), any(), any()
        );
    }

    private AdminOrderRefundServiceImpl service() {
        return new AdminOrderRefundServiceImpl(
                refundMapper,
                orderMapper,
                orderItemMapper,
                skuStockMapper,
                productMapper
        );
    }

    private boolean approveSucceeds(
            AdminOrderRefundServiceImpl service) {
        try {
            service.approve(900L, approveDTO());
            return true;
        } catch (BusinessException exception) {
            assertSame(
                    ErrorCode.REFUND_CONCURRENT_OPERATION,
                    exception.getErrorCode()
            );
            return false;
        }
    }

    private RefundApproveDTO approveDTO() {
        RefundApproveDTO dto = new RefundApproveDTO();
        dto.setAdminNote(" 审核通过 ");
        return dto;
    }

    private RefundRejectDTO rejectDTO() {
        RefundRejectDTO dto = new RefundRejectDTO();
        dto.setAdminNote(" 不符合退款条件 ");
        return dto;
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

    private OmsOrderItem item(Long skuId, int quantity) {
        OmsOrderItem item = new OmsOrderItem();
        item.setId(skuId + 1000);
        item.setOrderId(500L);
        item.setProductId(20L);
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        return item;
    }
}
