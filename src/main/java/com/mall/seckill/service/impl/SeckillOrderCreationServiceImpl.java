package com.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.member.entity.UmsMemberReceiveAddress;
import com.mall.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.order.config.OrderProperties;
import com.mall.order.entity.OmsOrder;
import com.mall.order.entity.OmsOrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.event.OrderCreatedEvent;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.support.OrderSnGenerator;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.seckill.entity.OmsSeckillOrder;
import com.mall.seckill.entity.SmsSeckillActivity;
import com.mall.seckill.entity.SmsSeckillSku;
import com.mall.seckill.enums.SeckillActivityStatus;
import com.mall.seckill.mapper.OmsSeckillOrderMapper;
import com.mall.seckill.mapper.SmsSeckillActivityMapper;
import com.mall.seckill.mapper.SmsSeckillSkuMapper;
import com.mall.seckill.mq.SeckillOrderMessage;
import com.mall.seckill.service.SeckillOrderCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SeckillOrderCreationServiceImpl
        implements SeckillOrderCreationService {

    private final OmsSeckillOrderMapper seckillOrderMapper;
    private final SmsSeckillSkuMapper seckillSkuMapper;
    private final SmsSeckillActivityMapper activityMapper;

    private final UmsMemberReceiveAddressMapper addressMapper;
    private final PmsProductMapper productMapper;
    private final PmsSkuStockMapper skuStockMapper;

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;

    private final OrderSnGenerator orderSnGenerator;
    private final OrderProperties orderProperties;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Long createOrder(
            SeckillOrderMessage message) {

        if (message.quantity() == null
                || message.quantity() != 1) {

            throw new BusinessException(
                    ErrorCode.PARAM_VALIDATION_FAILED
            );
        }

        /*
         * 消息重试或重复投递时，
         * 已经创建过订单就直接返回。
         */
        OmsSeckillOrder existing =
                seckillOrderMapper.selectOne(
                        new LambdaQueryWrapper<OmsSeckillOrder>()
                                .eq(
                                        OmsSeckillOrder::getRequestId,
                                        message.requestId()
                                )
                                .eq(
                                        OmsSeckillOrder::getSeckillSkuId,
                                        message.seckillSkuId()
                                )
                                .eq(
                                        OmsSeckillOrder::getMemberId,
                                        message.memberId()
                                )
                                .last("LIMIT 1")
                );

        if (existing != null) {
            return existing.getOrderId();
        }

        SmsSeckillSku seckillSku =
                seckillSkuMapper.selectById(
                        message.seckillSkuId()
                );

        if (seckillSku == null) {
            throw new BusinessException(
                    ErrorCode.SECKILL_SKU_NOT_FOUND
            );
        }

        SmsSeckillActivity activity =
                activityMapper.selectByIdForUpdate(
                        seckillSku.getActivityId()
                );

        if (activity == null) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_NOT_FOUND
            );
        }

        if (!Integer.valueOf(
                SeckillActivityStatus.ENABLED.getCode()
        ).equals(activity.getStatus())) {

            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_STATUS_INVALID
            );
        }

        UmsMemberReceiveAddress address =
                addressMapper.selectOne(
                        new LambdaQueryWrapper
                                <UmsMemberReceiveAddress>()
                                .eq(
                                        UmsMemberReceiveAddress::getId,
                                        message.addressId()
                                )
                                .eq(
                                        UmsMemberReceiveAddress::getMemberId,
                                        message.memberId()
                                )
                                .last("LIMIT 1")
                );

        if (address == null) {
            throw new BusinessException(
                    ErrorCode.ADDRESS_NOT_FOUND
            );
        }

        PmsSkuStock sku =
                skuStockMapper.selectById(
                        seckillSku.getSkuId()
                );

        if (sku == null) {
            throw new BusinessException(
                    ErrorCode.SKU_NOT_FOUND
            );
        }

        PmsProduct product =
                productMapper.selectById(
                        seckillSku.getProductId()
                );

        if (product == null
                || !seckillSku.getProductId()
                .equals(sku.getProductId())) {

            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        /*
         * Redis已经预扣一次；
         * 这里扣数据库秒杀库存，作为最终记录。
         */
        int stockUpdated =
                seckillSkuMapper
                        .decreaseAvailableStock(
                                seckillSku.getId(),
                                message.quantity()
                        );

        if (stockUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.STOCK_INSUFFICIENT
            );
        }

        BigDecimal amount =
                seckillSku.getSeckillPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        message.quantity()
                                )
                        );

        OmsOrder order = buildOrder(
                message,
                address,
                amount
        );

        if (orderMapper.insert(order) != 1) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        OmsOrderItem orderItem = buildOrderItem(
                order,
                product,
                sku,
                seckillSku,
                message.quantity(),
                amount
        );

        if (orderItemMapper.insert(orderItem) != 1) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        OmsSeckillOrder relation =
                new OmsSeckillOrder();

        relation.setRequestId(
                message.requestId()
        );
        relation.setOrderId(order.getId());
        relation.setActivityId(
                seckillSku.getActivityId()
        );
        relation.setSeckillSkuId(
                seckillSku.getId()
        );
        relation.setMemberId(
                message.memberId()
        );
        relation.setQuantity(
                message.quantity()
        );

        /*
         * 唯一索引冲突不能在这里吞掉。
         * 让异常触发事务回滚，消息重试后会查到原订单。
         */
        if (seckillOrderMapper.insert(relation) != 1) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        LocalDateTime expireTime =
                LocalDateTime.now()
                        .plusMinutes(
                                orderProperties
                                        .getTimeoutMinutes()
                        );

        eventPublisher.publishEvent(
                new OrderCreatedEvent(
                        order.getId(),
                        expireTime
                )
        );

        return order.getId();
    }

    private OmsOrder buildOrder(
            SeckillOrderMessage message,
            UmsMemberReceiveAddress address,
            BigDecimal amount) {

        OmsOrder order = new OmsOrder();

        order.setOrderSn(
                orderSnGenerator.generate()
        );

        order.setMemberId(
                message.memberId()
        );

        order.setSubmitToken(
                "SK:"
                        + message.seckillSkuId()
                        + ":"
                        + message.requestId()
        );

        order.setStatus(
                OrderStatus.PENDING_PAYMENT
        );

        order.setTotalAmount(amount);
        order.setPayAmount(amount);

        order.setReceiverName(
                address.getName()
        );

        order.setReceiverPhone(
                address.getPhoneNumber()
        );

        order.setReceiverPostCode(
                address.getPostCode()
        );

        order.setReceiverProvince(
                address.getProvince()
        );

        order.setReceiverCity(
                address.getCity()
        );

        order.setReceiverRegion(
                address.getRegion()
        );

        order.setReceiverDetailAddress(
                address.getDetailAddress()
        );

        order.setNote("秒杀订单");

        return order;
    }

    private OmsOrderItem buildOrderItem(
            OmsOrder order,
            PmsProduct product,
            PmsSkuStock sku,
            SmsSeckillSku seckillSku,
            Integer quantity,
            BigDecimal amount) {

        OmsOrderItem item = new OmsOrderItem();

        item.setOrderId(order.getId());
        item.setOrderSn(order.getOrderSn());

        item.setProductId(product.getId());
        item.setSkuId(sku.getId());
        item.setSkuCode(sku.getSkuCode());

        item.setProductName(product.getName());

        item.setProductPic(
                StringUtils.hasText(sku.getPic())
                        ? sku.getPic()
                        : product.getPic()
        );

        item.setSpecData(sku.getSpecData());

        item.setProductPrice(
                seckillSku.getSeckillPrice()
        );

        item.setQuantity(quantity);
        item.setSubtotal(amount);

        return item;
    }
}
