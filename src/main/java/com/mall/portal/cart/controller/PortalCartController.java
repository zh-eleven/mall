package com.mall.portal.cart.controller;

import com.mall.common.api.ApiResult;
import com.mall.portal.cart.dto.CartItemAddDTO;
import com.mall.portal.cart.service.PortalCartService;
import com.mall.portal.cart.vo.PortalCartItemVO;
import com.mall.security.MemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.mall.portal.cart.dto.CartItemQuantityUpdateDTO;
import com.mall.portal.cart.dto.CartItemSelectedUpdateDTO;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@RestController
@RequestMapping("/api/members/me/cart")
@RequiredArgsConstructor
@Validated
public class PortalCartController {

    private final PortalCartService cartService;

    @PostMapping
    public ApiResult<PortalCartItemVO> add(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @Valid @RequestBody
            CartItemAddDTO dto) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                cartService.add(memberId, dto),
                "商品已加入购物车"
        );
    }

    @GetMapping
    public ApiResult<List<PortalCartItemVO>> list(
            @AuthenticationPrincipal
            MemberDetails memberDetails) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                cartService.list(memberId)
        );
    }
    @PatchMapping("/{cartItemId}/quantity")
    public ApiResult<PortalCartItemVO> updateQuantity(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "购物车项ID必须大于0")
            Long cartItemId,

            @Valid @RequestBody
            CartItemQuantityUpdateDTO dto) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                cartService.updateQuantity(
                        memberId,
                        cartItemId,
                        dto
                ),
                "购物车商品数量修改成功"
        );
    }

    @PatchMapping("/{cartItemId}/selected")
    public ApiResult<PortalCartItemVO> updateSelected(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "购物车项ID必须大于0")
            Long cartItemId,

            @Valid @RequestBody
            CartItemSelectedUpdateDTO dto) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                cartService.updateSelected(
                        memberId,
                        cartItemId,
                        dto
                ),
                "购物车选中状态修改成功"
        );
    }

    @DeleteMapping("/{cartItemId}")
    public ApiResult<Void> delete(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "购物车项ID必须大于0")
            Long cartItemId) {

        Long memberId =
                memberDetails.getMember().getId();

        cartService.delete(memberId, cartItemId);

        return ApiResult.success(
                null,
                "购物车商品删除成功"
        );
    }
}