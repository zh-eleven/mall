package com.mall.portal.order.controller;

import com.mall.common.api.ApiResult;
import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.service.PortalOrderService;
import com.mall.portal.order.vo.OrderDetailVO;
import com.mall.portal.order.vo.OrderPreviewVO;
import com.mall.security.MemberDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mall.portal.order.dto.OrderSubmitDTO;
import com.mall.portal.order.vo.OrderSubmitVO;
import com.mall.common.api.PageResult;
import com.mall.portal.order.vo.OrderSummaryVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/members/me/orders")
@RequiredArgsConstructor
@Validated
public class PortalOrderController {

    private final PortalOrderService orderService;

    @PostMapping("/preview")
    public ApiResult<OrderPreviewVO> preview(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @Valid @RequestBody
            OrderPreviewDTO dto) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                orderService.preview(memberId, dto),
                "确认订单信息生成成功"
        );
    }

    @PostMapping
    public ApiResult<OrderSubmitVO> submit(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @Valid @RequestBody
            OrderSubmitDTO dto) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                orderService.submit(memberId, dto),
                "订单提交成功"
        );
    }

    @GetMapping("/{orderId}")
    public ApiResult<OrderDetailVO> detail(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "订单ID必须大于0")
            Long orderId) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                orderService.getDetail(
                        memberId,
                        orderId
                )
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResult<OrderDetailVO> cancel(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "订单ID必须大于0")
            Long orderId) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                orderService.cancel(
                        memberId,
                        orderId
                ),
                "订单取消成功"
        );
    }

    @PatchMapping("/{orderId}/pay")
    public ApiResult<OrderDetailVO> pay(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "订单ID必须大于0")
            Long orderId) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                orderService.pay(
                        memberId,
                        orderId
                ),
                "模拟支付成功"
        );
    }

    @GetMapping
    public ApiResult<PageResult<OrderSummaryVO>> page(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @RequestParam(required = false)
            @Min(value = 0, message = "订单状态不能小于0")
            @Max(value = 4, message = "订单状态不能大于4")
            Integer status,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码不能小于1")
            int pageNum,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量不能小于1")
            @Max(value = 100, message = "每页数量不能超过100")
            int pageSize) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                orderService.page(
                        memberId,
                        status,
                        pageNum,
                        pageSize
                )
        );
    }


}
