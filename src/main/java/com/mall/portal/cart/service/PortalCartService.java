package com.mall.portal.cart.service;

import com.mall.portal.cart.dto.CartItemAddDTO;
import com.mall.portal.cart.dto.CartItemQuantityUpdateDTO;
import com.mall.portal.cart.dto.CartItemSelectedUpdateDTO;
import com.mall.portal.cart.vo.PortalCartItemVO;

import java.util.List;

public interface PortalCartService {

    PortalCartItemVO add(
            Long memberId,
            CartItemAddDTO dto
    );

    List<PortalCartItemVO> list(Long memberId);

    PortalCartItemVO updateQuantity(
            Long memberId,
            Long cartItemId,
            CartItemQuantityUpdateDTO dto
    );

    PortalCartItemVO updateSelected(
            Long memberId,
            Long cartItemId,
            CartItemSelectedUpdateDTO dto
    );

    void delete(
            Long memberId,
            Long cartItemId
    );
}