package com.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query
        .LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.member.entity.UmsMemberReceiveAddress;
import com.mall.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.seckill.dto.SeckillSubmitDTO;
import com.mall.seckill.entity.OmsSeckillFailure;
import com.mall.seckill.entity.OmsSeckillOrder;
import com.mall.seckill.enums.SeckillReserveResult;
import com.mall.seckill.mapper.OmsSeckillFailureMapper;
import com.mall.seckill.mapper.OmsSeckillOrderMapper;
import com.mall.seckill.mq.SeckillOrderMessage;
import com.mall.seckill.mq.SeckillOrderMessagePublisher;
import com.mall.seckill.redis.SeckillRedisService;
import com.mall.seckill.service.SeckillPortalService;
import com.mall.seckill.vo.SeckillOrderStatusVO;
import com.mall.seckill.vo.SeckillSubmitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeckillPortalServiceImpl
        implements SeckillPortalService {

    private final UmsMemberReceiveAddressMapper
            addressMapper;

    private final SeckillRedisService
            seckillRedisService;

    private final SeckillOrderMessagePublisher
            messagePublisher;
    private final OmsSeckillOrderMapper
            seckillOrderMapper;

    private final OmsSeckillFailureMapper
            failureMapper;
    @Override
    public SeckillSubmitVO submit(
            Long memberId,
            Long seckillSkuId,
            SeckillSubmitDTO dto) {

        Long addressCount =
                addressMapper.selectCount(
                        new LambdaQueryWrapper
                                <UmsMemberReceiveAddress>()
                                .eq(
                                        UmsMemberReceiveAddress::getId,
                                        dto.getAddressId()
                                )
                                .eq(
                                        UmsMemberReceiveAddress::getMemberId,
                                        memberId
                                )
                );

        if (addressCount == 0) {
            throw new BusinessException(
                    ErrorCode.ADDRESS_NOT_FOUND
            );
        }

        String requestId =
                dto.getRequestId().trim();

        SeckillReserveResult result =
                seckillRedisService.reserve(
                        seckillSkuId,
                        requestId,
                        memberId,
                        dto.getAddressId(),
                        1
                );

        if (result
                == SeckillReserveResult
                .DUPLICATE_REQUEST) {

            /*
             * 同一requestId重试时不重复发消息，
             * 直接返回原来的排队状态。
             */
            return SeckillSubmitVO.queued(
                    requestId
            );
        }

        if (result != SeckillReserveResult.SUCCESS) {
            throw reserveException(result);
        }

        SeckillOrderMessage message =
                new SeckillOrderMessage(
                        requestId,
                        seckillSkuId,
                        memberId,
                        dto.getAddressId(),
                        1,
                        System.currentTimeMillis()
                );

        /*
         * 同步发送异常时Publisher会先回滚Redis，
         * 然后继续向上抛出异常。
         */
        messagePublisher.publish(message);

        return SeckillSubmitVO.queued(
                requestId
        );
    }

    private BusinessException reserveException(
            SeckillReserveResult result) {

        return switch (result) {
            case NOT_STARTED ->
                    new BusinessException(
                            ErrorCode.SECKILL_NOT_STARTED
                    );

            case ENDED ->
                    new BusinessException(
                            ErrorCode.SECKILL_ENDED
                    );

            case STOCK_INSUFFICIENT ->
                    new BusinessException(
                            ErrorCode.SECKILL_SOLD_OUT
                    );

            case LIMIT_EXCEEDED ->
                    new BusinessException(
                            ErrorCode.SECKILL_LIMIT_EXCEEDED
                    );

            case DATA_MISSING,
                 DUPLICATE_REQUEST,
                 SUCCESS ->
                    new BusinessException(
                            ErrorCode.SECKILL_DATA_UNAVAILABLE
                    );
        };
    }
    @Override
    public SeckillOrderStatusVO queryStatus(
            Long memberId,
            Long seckillSkuId,
            String requestId) {

        String normalizedRequestId =
                requestId.trim();

        OmsSeckillOrder seckillOrder =
                seckillOrderMapper.selectOne(
                        new LambdaQueryWrapper
                                <OmsSeckillOrder>()
                                .eq(
                                        OmsSeckillOrder::getRequestId,
                                        normalizedRequestId
                                )
                                .eq(
                                        OmsSeckillOrder::getMemberId,
                                        memberId
                                )
                                .eq(
                                        OmsSeckillOrder::getSeckillSkuId,
                                        seckillSkuId
                                )
                                .last("LIMIT 1")
                );

        if (seckillOrder != null) {
            return SeckillOrderStatusVO.created(
                    normalizedRequestId,
                    seckillOrder.getOrderId()
            );
        }

        OmsSeckillFailure failure =
                failureMapper.selectOne(
                        new LambdaQueryWrapper
                                <OmsSeckillFailure>()
                                .eq(
                                        OmsSeckillFailure::getRequestId,
                                        normalizedRequestId
                                )
                                .eq(
                                        OmsSeckillFailure::getMemberId,
                                        memberId
                                )
                                .eq(
                                        OmsSeckillFailure::getSeckillSkuId,
                                        seckillSkuId
                                )
                                .last("LIMIT 1")
                );

        if (failure != null) {
            if (Integer.valueOf(1)
                    .equals(failure.getStatus())) {

                return SeckillOrderStatusVO.failed(
                        normalizedRequestId
                );
            }

            return SeckillOrderStatusVO.compensating(
                    normalizedRequestId
            );
        }

        boolean queued =
                seckillRedisService.hasReservation(
                        seckillSkuId,
                        normalizedRequestId,
                        memberId
                );

        if (queued) {
            return SeckillOrderStatusVO.queued(
                    normalizedRequestId
            );
        }

        return SeckillOrderStatusVO.unknown(
                normalizedRequestId
        );
    }
}
