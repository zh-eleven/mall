package com.mall.portal.order.service;

import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.vo.OrderPreviewVO;

public interface PortalOrderService {

    OrderPreviewVO preview(
            Long memberId,
            OrderPreviewDTO dto
    );
}