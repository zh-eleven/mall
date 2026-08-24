package com.mall.order.service;

import com.mall.common.api.PageResult;
import com.mall.order.dto.AdminRefundQueryDTO;
import com.mall.order.dto.RefundApproveDTO;
import com.mall.order.dto.RefundRejectDTO;
import com.mall.order.vo.OrderRefundVO;

public interface AdminOrderRefundService {

    PageResult<OrderRefundVO> page(AdminRefundQueryDTO query);

    OrderRefundVO getDetail(Long refundId);

    OrderRefundVO approve(
            Long refundId,
            RefundApproveDTO dto
    );

    OrderRefundVO reject(
            Long refundId,
            RefundRejectDTO dto
    );
}
