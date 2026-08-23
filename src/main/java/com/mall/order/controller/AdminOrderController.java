package com.mall.order.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.order.dto.AdminOrderQueryDTO;
import com.mall.order.dto.OrderShipDTO;
import com.mall.order.service.AdminOrderService;
import com.mall.order.vo.AdminOrderDetailVO;
import com.mall.order.vo.AdminOrderSummaryVO;
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
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Validated
public class AdminOrderController {

    private final AdminOrderService orderService;

    @GetMapping
    @PreAuthorize("hasAuthority('order:read')")
    public ApiResult<PageResult<AdminOrderSummaryVO>> page(
            @Valid @ModelAttribute
            AdminOrderQueryDTO query) {

        return ApiResult.success(
                orderService.page(query)
        );
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('order:read')")
    public ApiResult<AdminOrderDetailVO> detail(
            @PathVariable
            @Positive(message = "订单ID必须大于0")
            Long orderId) {

        return ApiResult.success(
                orderService.getDetail(orderId)
        );
    }

    @PatchMapping("/{orderId}/ship")
    @PreAuthorize("hasAuthority('order:write')")
    public ApiResult<AdminOrderDetailVO> ship(
            @PathVariable
            @Positive(message = "订单ID必须大于0")
            Long orderId,

            @Valid @RequestBody
            OrderShipDTO dto) {

        return ApiResult.success(
                orderService.ship(orderId, dto),
                "订单发货成功"
        );
    }
}
