package com.mall.order.service;

import com.mall.common.api.PageResult;
import com.mall.order.dto.AdminOrderQueryDTO;
import com.mall.order.dto.OrderShipDTO;
import com.mall.order.vo.AdminOrderDetailVO;
import com.mall.order.vo.AdminOrderSummaryVO;

public interface AdminOrderService {

    PageResult<AdminOrderSummaryVO> page(
            AdminOrderQueryDTO query
    );

    AdminOrderDetailVO getDetail(Long orderId);

    AdminOrderDetailVO ship(
            Long orderId,
            OrderShipDTO dto
    );
}
