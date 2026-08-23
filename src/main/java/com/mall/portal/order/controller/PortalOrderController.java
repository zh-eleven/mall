package com.mall.portal.order.controller;

import com.mall.common.api.ApiResult;
import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.service.PortalOrderService;
import com.mall.portal.order.vo.OrderPreviewVO;
import com.mall.security.MemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/me/orders")
@RequiredArgsConstructor
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
}