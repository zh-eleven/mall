package com.mall.order.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.order.dto.AdminRefundQueryDTO;
import com.mall.order.dto.RefundApproveDTO;
import com.mall.order.dto.RefundRejectDTO;
import com.mall.order.service.AdminOrderRefundService;
import com.mall.order.vo.OrderRefundVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@Validated
public class AdminRefundController {

    private final AdminOrderRefundService refundService;

    @GetMapping
    @PreAuthorize("hasAuthority('refund:read')")
    public ApiResult<PageResult<OrderRefundVO>> page(
            @Valid @ModelAttribute AdminRefundQueryDTO query) {
        return ApiResult.success(refundService.page(query));
    }

    @GetMapping("/{refundId}")
    @PreAuthorize("hasAuthority('refund:read')")
    public ApiResult<OrderRefundVO> detail(
            @PathVariable
            @Positive(message = "退款申请ID必须大于0")
            Long refundId) {
        return ApiResult.success(
                refundService.getDetail(refundId)
        );
    }

    @PatchMapping("/{refundId}/approve")
    @PreAuthorize("hasAuthority('refund:write')")
    public ApiResult<OrderRefundVO> approve(
            @PathVariable
            @Positive(message = "退款申请ID必须大于0")
            Long refundId,
            @Valid @RequestBody RefundApproveDTO dto) {
        return ApiResult.success(
                refundService.approve(refundId, dto),
                "退款审核通过"
        );
    }

    @PatchMapping("/{refundId}/reject")
    @PreAuthorize("hasAuthority('refund:write')")
    public ApiResult<OrderRefundVO> reject(
            @PathVariable
            @Positive(message = "退款申请ID必须大于0")
            Long refundId,
            @Valid @RequestBody RefundRejectDTO dto) {
        return ApiResult.success(
                refundService.reject(refundId, dto),
                "退款审核拒绝"
        );
    }
}
