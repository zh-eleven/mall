package com.mall.portal.order.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.member.entity.UmsMemberReceiveAddress;
import com.mall.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.order.config.OrderProperties;
import com.mall.order.entity.OmsCartItem;
import com.mall.order.event.OrderCreatedEvent;
import com.mall.order.mapper.OmsCartItemMapper;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.OrderCancellationService;
import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.service.impl.PortalOrderServiceImpl;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.portal.order.dto.OrderSubmitDTO;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalOrderServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                UmsMemberReceiveAddress.class,
                OmsCartItem.class,
                OmsOrder.class,
                OmsOrderItem.class,
                PmsProduct.class,
                PmsSkuStock.class
        );
    }

    @Mock
    private OmsOrderMapper orderMapper;

    @Mock
    private OmsOrderItemMapper orderItemMapper;

    @Mock
    private UmsMemberReceiveAddressMapper addressMapper;

    @Mock
    private OmsCartItemMapper cartItemMapper;

    @Mock
    private PmsProductMapper productMapper;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private OrderProperties orderProperties;

    @Mock
    private OrderCancellationService
            orderCancellationService;

    @InjectMocks
    private PortalOrderServiceImpl orderService;

    @Test
    void previewShouldReturnReceiverItemsAndAmount() {

        UmsMemberReceiveAddress address = address();
        OmsCartItem cartItem = cartItem();
        PmsProduct product = product();
        PmsSkuStock sku = sku();

        stubPreviewData(
                address,
                cartItem,
                product,
                sku
        );

        var result = orderService.preview(
                4L,
                previewDTO()
        );

        assertEquals(
                30L,
                result.receiver().addressId()
        );

        assertEquals(1, result.items().size());
        assertEquals(2, result.totalQuantity());

        assertEquals(
                new BigDecimal("198.00"),
                result.totalAmount()
        );

        assertEquals(
                result.totalAmount(),
                result.payAmount()
        );

        var item = result.items().getFirst();

        assertEquals(100L, item.cartItemId());
        assertEquals(20L, item.productId());
        assertEquals(10L, item.skuId());
        assertEquals(2, item.quantity());

        assertEquals(
                new BigDecimal("198.00"),
                item.subtotal()
        );
    }

    @Test
    void previewShouldRejectAddressNotOwnedByMember() {

        when(addressMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.ADDRESS_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cartItemMapper,
                productMapper,
                skuStockMapper
        );
    }

    @Test
    void previewShouldRejectNoSelectedCartItems() {

        when(addressMapper.selectOne(any()))
                .thenReturn(address());

        when(cartItemMapper.selectList(any()))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.CART_NO_SELECTED_ITEMS,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                productMapper,
                skuStockMapper
        );
    }

    @Test
    void previewShouldRejectUnpublishedProduct() {

        PmsProduct product = product();
        product.setPublishStatus(0);

        stubPreviewData(
                address(),
                cartItem(),
                product,
                sku()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void previewShouldRejectInsufficientStock() {

        PmsSkuStock sku = sku();
        sku.setStock(2);
        sku.setLockedStock(1);

        stubPreviewData(
                address(),
                cartItem(),
                product(),
                sku
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.STOCK_INSUFFICIENT,
                exception.getErrorCode()
        );
    }

    @Test
    void previewShouldRejectSkuBelongingToAnotherProduct() {

        PmsSkuStock sku = sku();
        sku.setProductId(999L);

        stubPreviewData(
                address(),
                cartItem(),
                product(),
                sku
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.DATA_CONFLICT,
                exception.getErrorCode()
        );
    }

    private void stubPreviewData(
            UmsMemberReceiveAddress address,
            OmsCartItem cartItem,
            PmsProduct product,
            PmsSkuStock sku) {

        when(addressMapper.selectOne(any()))
                .thenReturn(address);

        when(cartItemMapper.selectList(any()))
                .thenReturn(List.of(cartItem));

        when(productMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(product));

        when(skuStockMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(sku));
    }

    private OrderPreviewDTO previewDTO() {
        OrderPreviewDTO dto = new OrderPreviewDTO();
        dto.setAddressId(30L);
        return dto;
    }

    private UmsMemberReceiveAddress address() {
        UmsMemberReceiveAddress address =
                new UmsMemberReceiveAddress();

        address.setId(30L);
        address.setMemberId(4L);
        address.setName("测试用户");
        address.setPhoneNumber("13800000000");
        address.setPostCode("100000");
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setRegion("海淀区");
        address.setDetailAddress("测试路1号");

        return address;
    }

    private OmsCartItem cartItem() {
        OmsCartItem item = new OmsCartItem();

        item.setId(100L);
        item.setMemberId(4L);
        item.setProductId(20L);
        item.setSkuId(10L);
        item.setQuantity(2);
        item.setSelected(1);

        return item;
    }

    private PmsProduct product() {
        PmsProduct product = new PmsProduct();

        product.setId(20L);
        product.setName("测试商品");
        product.setPic("product.jpg");
        product.setPublishStatus(1);

        return product;
    }

    private PmsSkuStock sku() {
        PmsSkuStock sku = new PmsSkuStock();

        sku.setId(10L);
        sku.setProductId(20L);
        sku.setSkuCode("TEST-SKU");
        sku.setPrice(new BigDecimal("99.00"));
        sku.setStock(10);
        sku.setLockedStock(0);
        sku.setSpecData("{}");

        return sku;
    }
    @Test
    void submitShouldCreateOrderLockStockAndDeleteCartItems() {

        stubPreviewData(
                address(),
                cartItem(),
                product(),
                sku()
        );

        when(skuStockMapper.lockStock(10L, 2))
                .thenReturn(1);

        when(orderMapper.insert(any(OmsOrder.class)))
                .thenAnswer(invocation -> {
                    OmsOrder order =
                            invocation.getArgument(0);

                    order.setId(500L);
                    return 1;
                });

        when(orderItemMapper.insert(
                any(OmsOrderItem.class)
        )).thenReturn(1);

        when(cartItemMapper.delete(any()))
                .thenReturn(1);

        when(orderProperties.getTimeoutMinutes())
                .thenReturn(30L);

        LocalDateTime beforeExpireTime =
                LocalDateTime.now().plusMinutes(30);

        var result = orderService.submit(
                4L,
                submitDTO()
        );

        assertEquals(500L, result.orderId());
        assertEquals(0, result.status());
        assertEquals(
                new BigDecimal("198.00"),
                result.payAmount()
        );

        verify(skuStockMapper).lockStock(
                10L,
                2
        );

        ArgumentCaptor<OmsOrder> orderCaptor =
                ArgumentCaptor.forClass(
                        OmsOrder.class
                );

        verify(orderMapper).insert(
                orderCaptor.capture()
        );

        OmsOrder insertedOrder =
                orderCaptor.getValue();

        assertEquals(4L, insertedOrder.getMemberId());
        assertEquals(
                "1234567890abcdef-submit",
                insertedOrder.getSubmitToken()
        );

        assertSame(
                OrderStatus.PENDING_PAYMENT,
                insertedOrder.getStatus()
        );

        assertEquals(
                new BigDecimal("198.00"),
                insertedOrder.getTotalAmount()
        );

        assertEquals(
                "测试备注",
                insertedOrder.getNote()
        );

        assertFalse(
                insertedOrder.getOrderSn().isBlank()
        );

        ArgumentCaptor<OmsOrderItem> itemCaptor =
                ArgumentCaptor.forClass(
                        OmsOrderItem.class
                );

        verify(orderItemMapper).insert(
                itemCaptor.capture()
        );

        OmsOrderItem insertedItem =
                itemCaptor.getValue();

        assertEquals(500L, insertedItem.getOrderId());
        assertEquals(20L, insertedItem.getProductId());
        assertEquals(10L, insertedItem.getSkuId());
        assertEquals(2, insertedItem.getQuantity());

        assertEquals(
                new BigDecimal("198.00"),
                insertedItem.getSubtotal()
        );

        verify(cartItemMapper).delete(any());

        ArgumentCaptor<OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        OrderCreatedEvent.class
                );

        verify(applicationEventPublisher).publishEvent(
                eventCaptor.capture()
        );

        InOrder writeOrder = inOrder(
                orderMapper,
                skuStockMapper
        );

        writeOrder.verify(orderMapper)
                .insert(any(OmsOrder.class));
        writeOrder.verify(skuStockMapper)
                .lockStock(10L, 2);

        OrderCreatedEvent event = eventCaptor.getValue();
        assertEquals(500L, event.orderId());
        assertFalse(
                event.expireTime().isBefore(
                        beforeExpireTime
                )
        );
        assertFalse(
                event.expireTime().isAfter(
                        LocalDateTime.now()
                                .plusMinutes(30)
                )
        );
    }

    @Test
    void submitShouldRejectWhenAtomicStockLockFails() {

        stubPreviewData(
                address(),
                cartItem(),
                product(),
                sku()
        );

        when(orderMapper.insert(any(OmsOrder.class)))
                .thenReturn(1);

        when(skuStockMapper.lockStock(10L, 2))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.submit(
                        4L,
                        submitDTO()
                )
        );

        assertSame(
                ErrorCode.STOCK_INSUFFICIENT,
                exception.getErrorCode()
        );

        verify(orderMapper)
                .insert(any(OmsOrder.class));

        verify(cartItemMapper, never())
                .delete(any());
    }

    @Test
    void submitShouldReturnExistingOrderForRepeatedToken() {

        OmsOrder existingOrder = pendingOrder();
        existingOrder.setSubmitToken(
                "1234567890abcdef-submit"
        );

        when(orderMapper.selectOne(any()))
                .thenReturn(existingOrder);

        var result = orderService.submit(
                4L,
                submitDTO()
        );

        assertEquals(500L, result.orderId());
        assertEquals("TEST-ORDER", result.orderSn());

        verify(orderMapper, never())
                .insert(any(OmsOrder.class));
        verifyNoInteractions(
                addressMapper,
                cartItemMapper,
                productMapper,
                skuStockMapper,
                orderItemMapper,
                applicationEventPublisher
        );
    }

    @Test
    void submitShouldReturnConcurrentOrderAfterUniqueKeyConflict() {

        stubPreviewData(
                address(),
                cartItem(),
                product(),
                sku()
        );

        OmsOrder concurrentOrder = pendingOrder();
        concurrentOrder.setSubmitToken(
                "1234567890abcdef-submit"
        );

        when(orderMapper.insert(any(OmsOrder.class)))
                .thenThrow(new DuplicateKeyException(
                        "duplicate submit token"
                ));

        when(orderMapper
                .selectByMemberIdAndSubmitTokenForUpdate(
                        4L,
                        "1234567890abcdef-submit"
                ))
                .thenReturn(concurrentOrder);

        var result = orderService.submit(
                4L,
                submitDTO()
        );

        assertEquals(500L, result.orderId());
        assertEquals("TEST-ORDER", result.orderSn());

        verify(skuStockMapper, never())
                .lockStock(anyLong(), anyInt());
        verify(orderItemMapper, never())
                .insert(any(OmsOrderItem.class));
        verify(cartItemMapper, never()).delete(any());
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void cancelShouldReturnCanceledOrderDetail() {

        OmsOrder canceledOrder = pendingOrder();

        canceledOrder.setStatus(
                OrderStatus.CANCELED
        );

        canceledOrder.setCancelTime(
                LocalDateTime.now()
        );

        when(orderMapper.selectOne(any()))
                .thenReturn(canceledOrder);

        when(orderItemMapper.selectList(any()))
                .thenReturn(
                        List.of(orderItem())
                );

        var result = orderService.cancel(
                4L,
                500L
        );

        assertEquals(4, result.status());
        assertEquals("已取消", result.statusDescription());
        assertNotNull(result.cancelTime());

        verify(orderCancellationService).cancelByMember(
                4L,
                500L
        );
    }

    @Test
    void cancelShouldPropagateInvalidStatus() {

        doThrow(new BusinessException(
                ErrorCode.ORDER_STATUS_INVALID
        )).when(orderCancellationService)
                .cancelByMember(4L, 500L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancel(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );

        verifyNoInteractions(orderItemMapper);
    }

    @Test
    void cancelShouldPropagateOrderNotFound() {

        doThrow(new BusinessException(
                ErrorCode.ORDER_NOT_FOUND
        )).when(orderCancellationService)
                .cancelByMember(4L, 500L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancel(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(orderItemMapper);
    }

    @Test
    void payShouldUpdateStatusAndDeductLockedStock() {

        OmsOrder pendingOrder = pendingOrder();
        OmsOrder paidOrder = pendingOrder();

        paidOrder.setStatus(
                OrderStatus.PENDING_SHIPMENT
        );

        paidOrder.setPaymentTime(
                LocalDateTime.now()
        );

        when(orderMapper.selectOne(any()))
                .thenReturn(
                        pendingOrder,
                        paidOrder
                );

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.payPendingOrder(
                500L,
                4L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PENDING_SHIPMENT.getCode()
        )).thenReturn(1);

        when(skuStockMapper.deductLockedStock(
                10L,
                2
        )).thenReturn(1);

        var result = orderService.pay(
                4L,
                500L
        );

        assertEquals(1, result.status());
        assertEquals("待发货", result.statusDescription());
        assertNotNull(result.paymentTime());

        verify(orderMapper).payPendingOrder(
                500L,
                4L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PENDING_SHIPMENT.getCode()
        );

        verify(skuStockMapper).deductLockedStock(
                10L,
                2
        );
    }

    @Test
    void payShouldRejectNonPendingOrder() {

        OmsOrder order = pendingOrder();

        order.setStatus(
                OrderStatus.PENDING_SHIPMENT
        );

        when(orderMapper.selectOne(any()))
                .thenReturn(order);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.pay(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );

        verify(orderMapper, never())
                .payPendingOrder(
                        anyLong(),
                        anyLong(),
                        anyInt(),
                        anyInt()
                );

        verify(skuStockMapper, never())
                .deductLockedStock(
                        anyLong(),
                        anyInt()
                );
    }

    @Test
    void payShouldRejectOrderNotOwnedByMember() {

        when(orderMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.pay(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(orderMapper, never())
                .payPendingOrder(
                        anyLong(),
                        anyLong(),
                        anyInt(),
                        anyInt()
                );

        verifyNoInteractions(orderItemMapper);
    }

    @Test
    void payShouldRejectConcurrentStatusChange() {

        when(orderMapper.selectOne(any()))
                .thenReturn(pendingOrder());

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.payPendingOrder(
                500L,
                4L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PENDING_SHIPMENT.getCode()
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.pay(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );

        verify(skuStockMapper, never())
                .deductLockedStock(
                        anyLong(),
                        anyInt()
                );
    }

    @Test
    void payShouldRejectInconsistentLockedStock() {

        when(orderMapper.selectOne(any()))
                .thenReturn(pendingOrder());

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        when(orderMapper.payPendingOrder(
                500L,
                4L,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PENDING_SHIPMENT.getCode()
        )).thenReturn(1);

        when(skuStockMapper.deductLockedStock(
                10L,
                2
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.pay(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.DATA_CONFLICT,
                exception.getErrorCode()
        );
    }

    @Test
    void confirmReceiptShouldCompleteShippedOrder() {

        OmsOrder shippedOrder = pendingOrder();
        shippedOrder.setStatus(OrderStatus.SHIPPED);

        OmsOrder completedOrder = pendingOrder();
        completedOrder.setStatus(OrderStatus.COMPLETED);
        completedOrder.setReceiveTime(LocalDateTime.now());

        when(orderMapper.selectOne(any()))
                .thenReturn(shippedOrder, completedOrder);

        when(orderMapper.confirmShippedOrder(
                500L,
                4L,
                OrderStatus.SHIPPED.getCode(),
                OrderStatus.COMPLETED.getCode()
        )).thenReturn(1);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        var result = orderService.confirmReceipt(
                4L,
                500L
        );

        assertEquals(3, result.status());
        assertEquals("已完成", result.statusDescription());
        assertNotNull(result.receiveTime());

        verify(orderMapper).confirmShippedOrder(
                500L,
                4L,
                OrderStatus.SHIPPED.getCode(),
                OrderStatus.COMPLETED.getCode()
        );
    }

    @Test
    void confirmReceiptShouldRejectOrderNotOwnedByMember() {

        when(orderMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.confirmReceipt(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(orderMapper, never())
                .confirmShippedOrder(
                        anyLong(),
                        anyLong(),
                        anyInt(),
                        anyInt()
                );
    }

    @Test
    void confirmReceiptShouldRejectNonShippedOrder() {

        when(orderMapper.selectOne(any()))
                .thenReturn(pendingOrder());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.confirmReceipt(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );

        verify(orderMapper, never())
                .confirmShippedOrder(
                        anyLong(),
                        anyLong(),
                        anyInt(),
                        anyInt()
                );
    }

    @Test
    void confirmReceiptShouldRejectConcurrentStatusChange() {

        OmsOrder shippedOrder = pendingOrder();
        shippedOrder.setStatus(OrderStatus.SHIPPED);

        when(orderMapper.selectOne(any()))
                .thenReturn(shippedOrder);

        when(orderMapper.confirmShippedOrder(
                500L,
                4L,
                OrderStatus.SHIPPED.getCode(),
                OrderStatus.COMPLETED.getCode()
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.confirmReceipt(
                        4L,
                        500L
                )
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );
    }

    private OrderSubmitDTO submitDTO() {
        OrderSubmitDTO dto = new OrderSubmitDTO();

        dto.setSubmitToken(
                "1234567890abcdef-submit"
        );
        dto.setAddressId(30L);
        dto.setNote("  测试备注  ");

        return dto;
    }

    private OmsOrder pendingOrder() {
        OmsOrder order = new OmsOrder();

        order.setId(500L);
        order.setOrderSn("TEST-ORDER");
        order.setMemberId(4L);
        order.setStatus(
                OrderStatus.PENDING_PAYMENT
        );

        order.setTotalAmount(
                new BigDecimal("198.00")
        );

        order.setPayAmount(
                new BigDecimal("198.00")
        );

        order.setReceiverName("测试用户");
        order.setReceiverPhone("13800000000");
        order.setReceiverPostCode("100000");
        order.setReceiverProvince("北京市");
        order.setReceiverCity("北京市");
        order.setReceiverRegion("海淀区");
        order.setReceiverDetailAddress("测试路1号");
        order.setCreateTime(LocalDateTime.now());

        return order;
    }

    private OmsOrderItem orderItem() {
        OmsOrderItem item = new OmsOrderItem();

        item.setId(600L);
        item.setOrderId(500L);
        item.setOrderSn("TEST-ORDER");
        item.setProductId(20L);
        item.setSkuId(10L);
        item.setSkuCode("TEST-SKU");
        item.setProductName("测试商品");
        item.setProductPic("product.jpg");
        item.setSpecData("{}");

        item.setProductPrice(
                new BigDecimal("99.00")
        );

        item.setQuantity(2);

        item.setSubtotal(
                new BigDecimal("198.00")
        );

        return item;
    }
}
