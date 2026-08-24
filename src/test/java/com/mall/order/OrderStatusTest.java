package com.mall.order;

import com.mall.order.entity.OmsOrder;
import com.mall.order.enums.OrderStatus;
import com.mall.order.vo.AdminOrderSummaryVO;
import com.mall.portal.order.vo.OrderSummaryVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStatusTest {

    @Test
    void existingCodesShouldStayStableAndRefundCodesShouldAppend() {
        assertEquals(0, OrderStatus.PENDING_PAYMENT.getCode());
        assertEquals(1, OrderStatus.PENDING_SHIPMENT.getCode());
        assertEquals(2, OrderStatus.SHIPPED.getCode());
        assertEquals(3, OrderStatus.COMPLETED.getCode());
        assertEquals(4, OrderStatus.CANCELED.getCode());
        assertEquals(5, OrderStatus.REFUNDING.getCode());
        assertEquals(6, OrderStatus.REFUNDED.getCode());
    }

    @Test
    void adminAndMemberVosShouldMapRefundStatuses() {
        OmsOrder order = new OmsOrder();
        order.setId(1L);
        order.setOrderSn("ORDER-1");
        order.setMemberId(2L);
        order.setStatus(OrderStatus.REFUNDED);
        order.setTotalAmount(BigDecimal.TEN);
        order.setPayAmount(BigDecimal.TEN);

        assertEquals(6,
                AdminOrderSummaryVO.from(order, List.of()).status());
        assertEquals("已退款",
                OrderSummaryVO.from(order, List.of())
                        .statusDescription());
    }
}
