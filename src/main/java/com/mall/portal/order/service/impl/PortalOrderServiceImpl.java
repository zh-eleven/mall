package com.mall.portal.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.member.entity.UmsMemberReceiveAddress;
import com.mall.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.order.entity.OmsCartItem;
import com.mall.order.mapper.OmsCartItemMapper;
import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.service.PortalOrderService;
import com.mall.portal.order.vo.*;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.OrderCancellationService;
import com.mall.portal.order.dto.OrderSubmitDTO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;

import java.util.UUID;
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
    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;

    private final OrderCancellationService
            orderCancellationService;


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

    @Override
    @Transactional
    public OrderSubmitVO submit(
            Long memberId,
            OrderSubmitDTO dto) {

        /*
         * 提交时重新生成预览，不能相信前端之前
         * 获取的价格、库存和地址信息。
         */
        OrderPreviewDTO previewDTO =
                new OrderPreviewDTO();

        previewDTO.setAddressId(dto.getAddressId());

        OrderPreviewVO preview =
                preview(memberId, previewDTO);

        lockStocks(preview.items());

        OmsOrder order = buildOrder(
                memberId,
                dto,
                preview
        );

        saveOrder(order);

        saveOrderItems(
                order,
                preview.items()
        );

        deleteSubmittedCartItems(
                memberId,
                preview.items()
        );

        return OrderSubmitVO.from(order);
    }

    private void lockStocks(
            List<OrderPreviewItemVO> items) {

        for (OrderPreviewItemVO item : items) {

            int updated = skuStockMapper.lockStock(
                    item.skuId(),
                    item.quantity()
            );

            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.STOCK_INSUFFICIENT
                );
            }
        }
    }

    private OmsOrder buildOrder(
            Long memberId,
            OrderSubmitDTO dto,
            OrderPreviewVO preview) {

        OrderReceiverVO receiver =
                preview.receiver();

        OmsOrder order = new OmsOrder();

        order.setOrderSn(generateOrderSn());
        order.setMemberId(memberId);
        order.setStatus(
                OrderStatus.PENDING_PAYMENT
        );

        order.setTotalAmount(
                preview.totalAmount()
        );

        order.setPayAmount(
                preview.payAmount()
        );

        order.setReceiverName(
                receiver.name()
        );

        order.setReceiverPhone(
                receiver.phoneNumber()
        );

        order.setReceiverPostCode(
                receiver.postCode()
        );

        order.setReceiverProvince(
                receiver.province()
        );

        order.setReceiverCity(
                receiver.city()
        );

        order.setReceiverRegion(
                receiver.region()
        );

        order.setReceiverDetailAddress(
                receiver.detailAddress()
        );

        order.setNote(
                StringUtils.hasText(dto.getNote())
                        ? dto.getNote().trim()
                        : null
        );

        return order;
    }

    private void saveOrder(OmsOrder order) {

        try {
            if (orderMapper.insert(order) != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT,
                    exception
            );
        }
    }

    private void saveOrderItems(
            OmsOrder order,
            List<OrderPreviewItemVO> items) {

        try {
            for (OrderPreviewItemVO item : items) {

                OmsOrderItem orderItem =
                        new OmsOrderItem();

                orderItem.setOrderId(order.getId());
                orderItem.setOrderSn(order.getOrderSn());

                orderItem.setProductId(
                        item.productId()
                );

                orderItem.setSkuId(
                        item.skuId()
                );

                orderItem.setSkuCode(
                        item.skuCode()
                );

                orderItem.setProductName(
                        item.productName()
                );

                orderItem.setProductPic(
                        item.pic()
                );

                orderItem.setSpecData(
                        item.specData()
                );

                orderItem.setProductPrice(
                        item.price()
                );

                orderItem.setQuantity(
                        item.quantity()
                );

                orderItem.setSubtotal(
                        item.subtotal()
                );

                if (orderItemMapper.insert(orderItem) != 1) {
                    throw new BusinessException(
                            ErrorCode.DATA_CONFLICT
                    );
                }
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT,
                    exception
            );
        }
    }

    private void deleteSubmittedCartItems(
            Long memberId,
            List<OrderPreviewItemVO> items) {

        List<Long> cartItemIds = items.stream()
                .map(OrderPreviewItemVO::cartItemId)
                .toList();

        int deleted = cartItemMapper.delete(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(
                                OmsCartItem::getMemberId,
                                memberId
                        )
                        .eq(
                                OmsCartItem::getSelected,
                                1
                        )
                        .in(
                                OmsCartItem::getId,
                                cartItemIds
                        )
        );

        if (deleted != cartItemIds.size()) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }
    }

    private String generateOrderSn() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    @Override
    public OrderDetailVO getDetail(
            Long memberId,
            Long orderId) {

        OmsOrder order = findOwnedOrder(
                memberId,
                orderId
        );

        List<OmsOrderItem> items =
                findOrderItems(orderId);

        return OrderDetailVO.from(
                order,
                items
        );
    }

    @Override
    @Transactional
    public OrderDetailVO cancel(
            Long memberId,
            Long orderId) {

        orderCancellationService.cancelByMember(
                memberId,
                orderId
        );

        return getDetail(
                memberId,
                orderId
        );
    }

    private OmsOrder findOwnedOrder(
            Long memberId,
            Long orderId) {

        OmsOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(
                                OmsOrder::getId,
                                orderId
                        )
                        .eq(
                                OmsOrder::getMemberId,
                                memberId
                        )
        );

        if (order == null) {
            throw new BusinessException(
                    ErrorCode.ORDER_NOT_FOUND
            );
        }

        return order;
    }

    private List<OmsOrderItem> findOrderItems(
            Long orderId) {

        List<OmsOrderItem> items =
                orderItemMapper.selectList(
                        new LambdaQueryWrapper<OmsOrderItem>()
                                .eq(
                                        OmsOrderItem::getOrderId,
                                        orderId
                                )
                                .orderByAsc(
                                        OmsOrderItem::getId
                                )
                );

        if (items.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        return items;
    }

    @Override
    public PageResult<OrderSummaryVO> page(
            Long memberId,
            Integer status,
            int pageNum,
            int pageSize) {

        LambdaQueryWrapper<OmsOrder> query =
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(
                                OmsOrder::getMemberId,
                                memberId
                        );

        if (status != null) {
            query.eq(
                    OmsOrder::getStatus,
                    status
            );
        }

        query.orderByDesc(OmsOrder::getCreateTime)
                .orderByDesc(OmsOrder::getId);

        Page<OmsOrder> page =
                orderMapper.selectPage(
                        new Page<>(pageNum, pageSize),
                        query
                );

        if (page.getRecords().isEmpty()) {
            return PageResult.from(
                    page,
                    order -> OrderSummaryVO.from(
                            order,
                            List.of()
                    )
            );
        }

        List<Long> orderIds = page.getRecords()
                .stream()
                .map(OmsOrder::getId)
                .toList();

        Map<Long, List<OmsOrderItem>> itemsByOrder =
                orderItemMapper.selectList(
                                new LambdaQueryWrapper<OmsOrderItem>()
                                        .in(
                                                OmsOrderItem::getOrderId,
                                                orderIds
                                        )
                                        .orderByAsc(
                                                OmsOrderItem::getId
                                        )
                        )
                        .stream()
                        .collect(Collectors.groupingBy(
                                OmsOrderItem::getOrderId
                        ));

        return PageResult.from(
                page,
                order -> OrderSummaryVO.from(
                        order,
                        itemsByOrder.getOrDefault(
                                order.getId(),
                                List.of()
                        )
                )
        );
    }

    @Override
    @Transactional
    public OrderDetailVO pay(
            Long memberId,
            Long orderId) {

        OmsOrder order = findOwnedOrder(
                memberId,
                orderId
        );

        if (!OrderStatus.PENDING_PAYMENT.equals(
                order.getStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        List<OmsOrderItem> items =
                findOrderItems(orderId);

        int updated = orderMapper.payPendingOrder(
                orderId,
                memberId,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PENDING_SHIPMENT.getCode()
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        deductLockedStocks(items);

        return getDetail(
                memberId,
                orderId
        );
    }

    private void deductLockedStocks(
            List<OmsOrderItem> items) {

        for (OmsOrderItem item : items) {

            int updated =
                    skuStockMapper.deductLockedStock(
                            item.getSkuId(),
                            item.getQuantity()
                    );

            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }
        }
    }

    @Override
    @Transactional
    public OrderDetailVO confirmReceipt(
            Long memberId,
            Long orderId) {

        OmsOrder order = findOwnedOrder(
                memberId,
                orderId
        );

        if (!OrderStatus.SHIPPED.equals(
                order.getStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        int updated = orderMapper.confirmShippedOrder(
                orderId,
                memberId,
                OrderStatus.SHIPPED.getCode(),
                OrderStatus.COMPLETED.getCode()
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        return getDetail(
                memberId,
                orderId
        );
    }

}
