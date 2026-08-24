package com.mall.order.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.order.dto.MemberRefundQueryDTO;
import com.mall.order.dto.RefundApplyDTO;
import com.mall.order.service.OrderRefundService;
import com.mall.order.vo.OrderRefundVO;
import com.mall.security.MemberDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
@Validated
public class MemberRefundController {

    private final OrderRefundService refundService;

    @PostMapping("/orders/{orderId}/refunds")
    public ApiResult<OrderRefundVO> apply(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable
            @Positive(message = "订单ID必须大于0")
            Long orderId,
            @Valid @RequestBody RefundApplyDTO dto) {

        Long memberId = memberDetails.getMember().getId();

        return ApiResult.success(
                refundService.apply(memberId, orderId, dto),
                "退款申请提交成功"
        );
    }

    @GetMapping("/refunds")
    public ApiResult<PageResult<OrderRefundVO>> page(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Valid @ModelAttribute MemberRefundQueryDTO query) {

        Long memberId = memberDetails.getMember().getId();

        return ApiResult.success(
                refundService.page(memberId, query)
        );
    }

    @GetMapping("/refunds/{refundId}")
    public ApiResult<OrderRefundVO> detail(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable
            @Positive(message = "退款申请ID必须大于0")
            Long refundId) {

        Long memberId = memberDetails.getMember().getId();

        return ApiResult.success(
                refundService.getDetail(memberId, refundId)
        );
    }
}
