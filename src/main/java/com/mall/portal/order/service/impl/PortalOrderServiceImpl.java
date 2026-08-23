package com.mall.portal.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.member.entity.UmsMemberReceiveAddress;
import com.mall.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.portal.order.entity.OmsCartItem;
import com.mall.portal.order.mapper.OmsCartItemMapper;
import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.service.PortalOrderService;
import com.mall.portal.order.vo.OrderPreviewItemVO;
import com.mall.portal.order.vo.OrderPreviewVO;
import com.mall.portal.order.vo.OrderReceiverVO;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalOrderServiceImpl
        implements PortalOrderService {

    private final UmsMemberReceiveAddressMapper addressMapper;
    private final OmsCartItemMapper cartItemMapper;
    private final PmsProductMapper productMapper;
    private final PmsSkuStockMapper skuStockMapper;

    @Override
    public OrderPreviewVO preview(
            Long memberId,
            OrderPreviewDTO dto) {

        UmsMemberReceiveAddress address =
                findOwnedAddress(
                        memberId,
                        dto.getAddressId()
                );

        List<OmsCartItem> cartItems =
                findSelectedCartItems(memberId);

        Map<Long, PmsProduct> productMap =
                findProductMap(cartItems);

        Map<Long, PmsSkuStock> skuMap =
                findSkuMap(cartItems);

        List<OrderPreviewItemVO> items =
                cartItems.stream()
                        .map(cartItem ->
                                buildPreviewItem(
                                        cartItem,
                                        productMap,
                                        skuMap
                                )
                        )
                        .toList();

        return OrderPreviewVO.from(
                OrderReceiverVO.from(address),
                items
        );
    }

    private UmsMemberReceiveAddress findOwnedAddress(
            Long memberId,
            Long addressId) {

        UmsMemberReceiveAddress address =
                addressMapper.selectOne(
                        new LambdaQueryWrapper<
                                UmsMemberReceiveAddress>()
                                .eq(
                                        UmsMemberReceiveAddress::getId,
                                        addressId
                                )
                                .eq(
                                        UmsMemberReceiveAddress::getMemberId,
                                        memberId
                                )
                );

        if (address == null) {
            throw new BusinessException(
                    ErrorCode.ADDRESS_NOT_FOUND
            );
        }

        return address;
    }

    private List<OmsCartItem> findSelectedCartItems(
            Long memberId) {

        List<OmsCartItem> cartItems =
                cartItemMapper.selectList(
                        new LambdaQueryWrapper<OmsCartItem>()
                                .eq(
                                        OmsCartItem::getMemberId,
                                        memberId
                                )
                                .eq(
                                        OmsCartItem::getSelected,
                                        1
                                )
                                .orderByAsc(
                                        OmsCartItem::getId
                                )
                );

        if (cartItems.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.CART_NO_SELECTED_ITEMS
            );
        }

        return cartItems;
    }

    private Map<Long, PmsProduct> findProductMap(
            List<OmsCartItem> cartItems) {

        List<Long> productIds = cartItems.stream()
                .map(OmsCartItem::getProductId)
                .distinct()
                .toList();

        return productMapper.selectBatchIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        PmsProduct::getId,
                        Function.identity()
                ));
    }

    private Map<Long, PmsSkuStock> findSkuMap(
            List<OmsCartItem> cartItems) {

        List<Long> skuIds = cartItems.stream()
                .map(OmsCartItem::getSkuId)
                .distinct()
                .toList();

        return skuStockMapper.selectBatchIds(skuIds)
                .stream()
                .collect(Collectors.toMap(
                        PmsSkuStock::getId,
                        Function.identity()
                ));
    }

    private OrderPreviewItemVO buildPreviewItem(
            OmsCartItem cartItem,
            Map<Long, PmsProduct> productMap,
            Map<Long, PmsSkuStock> skuMap) {

        PmsProduct product =
                productMap.get(cartItem.getProductId());

        if (product == null
                || !Integer.valueOf(1).equals(
                product.getPublishStatus()
        )) {

            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        PmsSkuStock sku =
                skuMap.get(cartItem.getSkuId());

        if (sku == null) {
            throw new BusinessException(
                    ErrorCode.SKU_NOT_FOUND
            );
        }

        if (!product.getId().equals(
                sku.getProductId()
        )) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        Integer quantity = cartItem.getQuantity();

        if (quantity == null || quantity < 1) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        int stock = sku.getStock() == null
                ? 0
                : sku.getStock();

        int lockedStock = sku.getLockedStock() == null
                ? 0
                : sku.getLockedStock();

        int availableStock = Math.max(
                stock - lockedStock,
                0
        );

        if (availableStock < quantity) {
            throw new BusinessException(
                    ErrorCode.STOCK_INSUFFICIENT
            );
        }

        return OrderPreviewItemVO.from(
                cartItem,
                product,
                sku
        );
    }
}