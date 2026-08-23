package com.mall.portal.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.portal.order.entity.OmsCartItem;
import com.mall.portal.order.mapper.OmsCartItemMapper;
import com.mall.portal.cart.dto.CartItemAddDTO;
import com.mall.portal.cart.service.PortalCartService;
import com.mall.portal.cart.vo.PortalCartItemVO;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mall.portal.cart.dto.CartItemQuantityUpdateDTO;
import com.mall.portal.cart.dto.CartItemSelectedUpdateDTO;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalCartServiceImpl
        implements PortalCartService {

    private final OmsCartItemMapper cartItemMapper;
    private final PmsProductMapper productMapper;
    private final PmsSkuStockMapper skuStockMapper;

    @Override
    @Transactional
    public PortalCartItemVO add(
            Long memberId,
            CartItemAddDTO dto) {

        PmsSkuStock sku =
                skuStockMapper.selectById(dto.getSkuId());

        if (sku == null) {
            throw new BusinessException(
                    ErrorCode.SKU_NOT_FOUND
            );
        }

        PmsProduct product = productMapper.selectOne(
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(PmsProduct::getId, sku.getProductId())
                        .eq(PmsProduct::getPublishStatus, 1)
        );

        if (product == null) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        OmsCartItem cartItem = cartItemMapper.selectOne(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getMemberId, memberId)
                        .eq(OmsCartItem::getSkuId, sku.getId())
        );

        int currentQuantity = cartItem == null
                ? 0
                : cartItem.getQuantity();

        int targetQuantity =
                currentQuantity + dto.getQuantity();

        if (targetQuantity > 999) {
            throw new BusinessException(
                    ErrorCode.PARAM_VALIDATION_FAILED,
                    "购物车中单个SKU数量不能超过999"
            );
        }

        int availableStock = Math.max(
                sku.getStock() - sku.getLockedStock(),
                0
        );

        if (targetQuantity > availableStock) {
            throw new BusinessException(
                    ErrorCode.STOCK_INSUFFICIENT
            );
        }

        if (cartItem == null) {
            cartItem = new OmsCartItem();
            cartItem.setMemberId(memberId);
            cartItem.setProductId(product.getId());
            cartItem.setSkuId(sku.getId());
            cartItem.setQuantity(targetQuantity);
            cartItem.setSelected(1);

            try {
                cartItemMapper.insert(cartItem);
            } catch (DuplicateKeyException exception) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT,
                        exception
                );
            }
        } else {
            int updated = cartItemMapper.update(
                    null,
                    new LambdaUpdateWrapper<OmsCartItem>()
                            .eq(
                                    OmsCartItem::getId,
                                    cartItem.getId()
                            )
                            .eq(
                                    OmsCartItem::getQuantity,
                                    currentQuantity
                            )
                            .set(
                                    OmsCartItem::getQuantity,
                                    targetQuantity
                            )
                            .set(
                                    OmsCartItem::getSelected,
                                    1
                            )
            );

            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }

            cartItem.setQuantity(targetQuantity);
            cartItem.setSelected(1);
        }

        return PortalCartItemVO.from(
                cartItem,
                product,
                sku
        );
    }

    @Override
    public List<PortalCartItemVO> list(Long memberId) {

        List<OmsCartItem> cartItems =
                cartItemMapper.selectList(
                        new LambdaQueryWrapper<OmsCartItem>()
                                .eq(
                                        OmsCartItem::getMemberId,
                                        memberId
                                )
                                .orderByDesc(
                                        OmsCartItem::getId
                                )
                );

        if (cartItems.isEmpty()) {
            return List.of();
        }

        Map<Long, PmsProduct> productMap =
                productMapper.selectBatchIds(
                                cartItems.stream()
                                        .map(OmsCartItem::getProductId)
                                        .distinct()
                                        .toList()
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                PmsProduct::getId,
                                Function.identity()
                        ));

        Map<Long, PmsSkuStock> skuMap =
                skuStockMapper.selectBatchIds(
                                cartItems.stream()
                                        .map(OmsCartItem::getSkuId)
                                        .distinct()
                                        .toList()
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                PmsSkuStock::getId,
                                Function.identity()
                        ));

        return cartItems.stream()
                .filter(item ->
                        productMap.containsKey(item.getProductId())
                                && skuMap.containsKey(item.getSkuId())
                )
                .map(item -> PortalCartItemVO.from(
                        item,
                        productMap.get(item.getProductId()),
                        skuMap.get(item.getSkuId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public PortalCartItemVO updateQuantity(
            Long memberId,
            Long cartItemId,
            CartItemQuantityUpdateDTO dto) {

        OmsCartItem cartItem =
                findOwnedCartItem(memberId, cartItemId);

        PmsSkuStock sku =
                findSku(cartItem.getSkuId());

        PmsProduct product =
                findProduct(cartItem.getProductId());

        if (!Integer.valueOf(1).equals(
                product.getPublishStatus())) {

            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        if (!product.getId().equals(sku.getProductId())) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        int availableStock = Math.max(
                sku.getStock() - sku.getLockedStock(),
                0
        );

        if (dto.getQuantity() > availableStock) {
            throw new BusinessException(
                    ErrorCode.STOCK_INSUFFICIENT
            );
        }

        if (!dto.getQuantity().equals(
                cartItem.getQuantity())) {

            int updated = cartItemMapper.update(
                    null,
                    new LambdaUpdateWrapper<OmsCartItem>()
                            .eq(
                                    OmsCartItem::getId,
                                    cartItemId
                            )
                            .eq(
                                    OmsCartItem::getMemberId,
                                    memberId
                            )
                            .eq(
                                    OmsCartItem::getQuantity,
                                    cartItem.getQuantity()
                            )
                            .set(
                                    OmsCartItem::getQuantity,
                                    dto.getQuantity()
                            )
            );

            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }

            cartItem.setQuantity(dto.getQuantity());
        }

        return PortalCartItemVO.from(
                cartItem,
                product,
                sku
        );
    }

    @Override
    @Transactional
    public PortalCartItemVO updateSelected(
            Long memberId,
            Long cartItemId,
            CartItemSelectedUpdateDTO dto) {

        OmsCartItem cartItem =
                findOwnedCartItem(memberId, cartItemId);

        int selected = Boolean.TRUE.equals(
                dto.getSelected()
        ) ? 1 : 0;

        if (!Integer.valueOf(selected).equals(
                cartItem.getSelected())) {

            int updated = cartItemMapper.update(
                    null,
                    new LambdaUpdateWrapper<OmsCartItem>()
                            .eq(
                                    OmsCartItem::getId,
                                    cartItemId
                            )
                            .eq(
                                    OmsCartItem::getMemberId,
                                    memberId
                            )
                            .set(
                                    OmsCartItem::getSelected,
                                    selected
                            )
            );

            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }

            cartItem.setSelected(selected);
        }

        return PortalCartItemVO.from(
                cartItem,
                findProduct(cartItem.getProductId()),
                findSku(cartItem.getSkuId())
        );
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long cartItemId) {

        int deleted = cartItemMapper.delete(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getId, cartItemId)
                        .eq(OmsCartItem::getMemberId, memberId)
        );

        if (deleted != 1) {
            throw new BusinessException(
                    ErrorCode.CART_ITEM_NOT_FOUND
            );
        }
    }

    private OmsCartItem findOwnedCartItem(
            Long memberId,
            Long cartItemId) {

        OmsCartItem cartItem = cartItemMapper.selectOne(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getId, cartItemId)
                        .eq(OmsCartItem::getMemberId, memberId)
        );

        if (cartItem == null) {
            throw new BusinessException(
                    ErrorCode.CART_ITEM_NOT_FOUND
            );
        }

        return cartItem;
    }

    private PmsProduct findProduct(Long productId) {

        PmsProduct product =
                productMapper.selectById(productId);

        if (product == null) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        return product;
    }

    private PmsSkuStock findSku(Long skuId) {

        PmsSkuStock sku =
                skuStockMapper.selectById(skuId);

        if (sku == null) {
            throw new BusinessException(
                    ErrorCode.SKU_NOT_FOUND
            );
        }

        return sku;
    }
}