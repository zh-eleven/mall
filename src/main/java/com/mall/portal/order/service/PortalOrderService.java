package com.mall.portal.order.service;

import com.mall.common.api.PageResult;
import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.dto.OrderSubmitDTO;
import com.mall.portal.order.vo.OrderDetailVO;
import com.mall.portal.order.vo.OrderPreviewVO;
import com.mall.portal.order.vo.OrderSubmitVO;
import com.mall.portal.order.vo.OrderSummaryVO;

public interface PortalOrderService {

    OrderPreviewVO preview(
            Long memberId,
            OrderPreviewDTO dto
    );

    OrderSubmitVO submit(
            Long memberId,
            OrderSubmitDTO dto
    );
    OrderDetailVO getDetail(
            Long memberId,
            Long orderId
    );

    OrderDetailVO cancel(
            Long memberId,
            Long orderId
    );

    PageResult<OrderSummaryVO> page(
            Long memberId,
            Integer status,
            int pageNum,
            int pageSize
    );

    OrderDetailVO pay(Long memberId, Long orderId);
}