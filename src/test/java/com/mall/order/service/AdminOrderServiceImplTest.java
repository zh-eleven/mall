package com.mall.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.order.dto.AdminOrderQueryDTO;
import com.mall.order.dto.OrderShipDTO;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.impl.AdminOrderServiceImpl;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {

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

    @InjectMocks
    private AdminOrderServiceImpl orderService;

    @Test
    void pageShouldReturnOrderAndProductSummary() {

        Page<OmsOrder> data = new Page<>(1, 10);
        data.setRecords(List.of(order(
                OrderStatus.PENDING_SHIPMENT
        )));
        data.setTotal(1);

        when(orderMapper.selectPage(any(), any()))
                .thenReturn(data);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        AdminOrderQueryDTO query =
                new AdminOrderQueryDTO();
        query.setOrderSn("TEST");
        query.setMemberId(4L);
        query.setStatus(1);

        var result = orderService.page(query);

        assertEquals(1, result.total());
        assertEquals(1, result.list().size());
        assertEquals(
                "TEST-ORDER",
                result.list().getFirst().orderSn()
        );
        assertEquals(
                "测试商品",
                result.list().getFirst().firstProductName()
        );
        assertEquals(
                2,
                result.list().getFirst().totalQuantity()
        );
    }

    @Test
    void detailShouldReturnReceiverDeliveryAndAllItems() {

        OmsOrder order = order(OrderStatus.SHIPPED);
        order.setDeliveryCompany("顺丰速运");
        order.setDeliverySn("SF123456789");
        order.setDeliveryTime(LocalDateTime.now());

        when(orderMapper.selectById(500L))
                .thenReturn(order);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        var result = orderService.getDetail(500L);

        assertEquals(500L, result.orderId());
        assertEquals("测试用户", result.receiver().name());
        assertEquals(
                "SF123456789",
                result.delivery().deliverySn()
        );
        assertEquals(1, result.items().size());
    }

    @Test
    void detailShouldRejectMissingOrder() {

        when(orderMapper.selectById(500L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.getDetail(500L)
        );

        assertSame(
                ErrorCode.ORDER_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void shipShouldUpdatePendingShipmentOrder() {

        OmsOrder pending = order(
                OrderStatus.PENDING_SHIPMENT
        );
        OmsOrder shipped = order(OrderStatus.SHIPPED);
        shipped.setDeliveryCompany("顺丰速运");
        shipped.setDeliverySn("SF123456789");
        shipped.setDeliveryTime(LocalDateTime.now());

        when(orderMapper.selectById(500L))
                .thenReturn(pending, shipped);

        when(orderMapper.shipPendingOrder(
                500L,
                "顺丰速运",
                "SF123456789",
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.SHIPPED.getCode()
        )).thenReturn(1);

        when(orderItemMapper.selectList(any()))
                .thenReturn(List.of(orderItem()));

        var result = orderService.ship(
                500L,
                shipDTO()
        );

        assertEquals(2, result.status());
        assertNotNull(result.delivery().deliveryTime());

        verify(orderMapper).shipPendingOrder(
                500L,
                "顺丰速运",
                "SF123456789",
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.SHIPPED.getCode()
        );
    }

    @Test
    void shipShouldRejectNonPendingShipmentOrder() {

        when(orderMapper.selectById(500L))
                .thenReturn(order(OrderStatus.SHIPPED));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.ship(
                        500L,
                        shipDTO()
                )
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );

        verify(orderMapper, never())
                .shipPendingOrder(
                        anyLong(),
                        any(),
                        any(),
                        anyInt(),
                        anyInt()
                );
    }

    @Test
    void shipShouldRejectConcurrentStatusChange() {

        when(orderMapper.selectById(500L))
                .thenReturn(order(
                        OrderStatus.PENDING_SHIPMENT
                ));

        when(orderMapper.shipPendingOrder(
                500L,
                "顺丰速运",
                "SF123456789",
                OrderStatus.PENDING_SHIPMENT.getCode(),
                OrderStatus.SHIPPED.getCode()
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.ship(
                        500L,
                        shipDTO()
                )
        );

        assertSame(
                ErrorCode.ORDER_STATUS_INVALID,
                exception.getErrorCode()
        );
    }

    private OrderShipDTO shipDTO() {
        OrderShipDTO dto = new OrderShipDTO();
        dto.setDeliveryCompany(" 顺丰速运 ");
        dto.setDeliverySn(" SF123456789 ");
        return dto;
    }

    private OmsOrder order(OrderStatus status) {
        OmsOrder order = new OmsOrder();
        order.setId(500L);
        order.setOrderSn("TEST-ORDER");
        order.setMemberId(4L);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("198.00"));
        order.setPayAmount(new BigDecimal("198.00"));
        order.setReceiverName("测试用户");
        order.setReceiverPhone("13800000000");
        order.setReceiverPostCode("100000");
        order.setReceiverProvince("北京市");
        order.setReceiverCity("北京市");
        order.setReceiverRegion("海淀区");
        order.setReceiverDetailAddress("测试路1号");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
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
        item.setProductPrice(new BigDecimal("99.00"));
        item.setQuantity(2);
        item.setSubtotal(new BigDecimal("198.00"));
        return item;
    }
}
