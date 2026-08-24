package com.mall.order.service;

import com.mall.common.api.PageResult;
import com.mall.order.dto.MemberRefundQueryDTO;
import com.mall.order.dto.RefundApplyDTO;
import com.mall.order.vo.OrderRefundVO;

public interface OrderRefundService {

    OrderRefundVO apply(
            Long memberId,
            Long orderId,
            RefundApplyDTO dto
    );

    PageResult<OrderRefundVO> page(
            Long memberId,
            MemberRefundQueryDTO query
    );

    OrderRefundVO getDetail(
            Long memberId,
            Long refundId
    );
}
