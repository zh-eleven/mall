package com.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.entity.PmsProduct;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.seckill.dto.SeckillActivityCreateDTO;
import com.mall.seckill.entity.SmsSeckillActivity;
import com.mall.seckill.enums.SeckillActivityStatus;
import com.mall.seckill.mapper.SmsSeckillActivityMapper;
import com.mall.seckill.redis.SeckillRedisService;
import com.mall.seckill.service.SeckillAdminService;
import com.mall.seckill.vo.SeckillActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.seckill.dto.SeckillSkuCreateDTO;
import com.mall.seckill.entity.SmsSeckillSku;
import com.mall.seckill.mapper.SmsSeckillSkuMapper;
import com.mall.seckill.vo.SeckillSkuVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class SeckillAdminServiceImpl
        implements SeckillAdminService {
    private final PmsProductMapper productMapper;

    private final SeckillRedisService seckillRedisService;
    private final SmsSeckillActivityMapper activityMapper;
    private final SmsSeckillSkuMapper seckillSkuMapper;

    private final PmsSkuStockMapper skuStockMapper;

    @Override
    public SeckillActivityVO createActivity(
            SeckillActivityCreateDTO dto) {

        /*
         * @Future 只能保证两个时间都在未来，
         * 这里继续保证结束时间晚于开始时间。
         */
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_TIME_INVALID
            );
        }

        SmsSeckillActivity activity =
                new SmsSeckillActivity();

        activity.setName(dto.getName().trim());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());

        // 新建活动不能直接启用。
        activity.setStatus(
                SeckillActivityStatus.DISABLED.getCode()
        );

        int inserted = activityMapper.insert(activity);

        if (inserted != 1) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        return getActivity(activity.getId());
    }

    @Override
    public SeckillActivityVO getActivity(Long activityId) {

        SmsSeckillActivity activity =
                activityMapper.selectById(activityId);

        if (activity == null) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_NOT_FOUND
            );
        }

        return SeckillActivityVO.from(activity);
    }

    @Override
    @Transactional
    public SeckillSkuVO addSku(
            Long activityId,
            SeckillSkuCreateDTO dto) {

        SmsSeckillActivity activity =
                activityMapper.selectById(activityId);

        if (activity == null) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_NOT_FOUND
            );
        }

        /*
         * 已启用或已经开始的活动不允许继续修改商品，
         * 避免数据库配置与 Redis 预热数据不一致。
         */
        if (!Integer.valueOf(
                SeckillActivityStatus.DISABLED.getCode()
        ).equals(activity.getStatus())
                || !activity.getStartTime()
                .isAfter(LocalDateTime.now())) {

            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_STATUS_INVALID
            );
        }

        PmsSkuStock sku =
                skuStockMapper.selectById(dto.getSkuId());

        if (sku == null) {
            throw new BusinessException(
                    ErrorCode.SKU_NOT_FOUND
            );
        }

        /*
         * 秒杀价格不能高于SKU正常销售价格。
         */
        if (dto.getSeckillPrice()
                .compareTo(sku.getPrice()) > 0) {

            throw new BusinessException(
                    ErrorCode.SECKILL_PRICE_INVALID
            );
        }

        int skuAvailableStock =
                sku.getStock() - sku.getLockedStock();

        /*
         * 秒杀活动库存只是SKU真实可用库存的一部分。
         */
        if (dto.getTotalStock() > skuAvailableStock) {
            throw new BusinessException(
                    ErrorCode.SECKILL_STOCK_INVALID
            );
        }

        int perUserLimit =
                dto.getPerUserLimit() == null
                        ? 1
                        : dto.getPerUserLimit();

        if (perUserLimit > dto.getTotalStock()) {
            throw new BusinessException(
                    ErrorCode.SECKILL_STOCK_INVALID
            );
        }

        SmsSeckillSku seckillSku =
                new SmsSeckillSku();

        seckillSku.setActivityId(activityId);
        seckillSku.setProductId(sku.getProductId());
        seckillSku.setSkuId(sku.getId());
        seckillSku.setSeckillPrice(
                dto.getSeckillPrice()
        );
        seckillSku.setTotalStock(
                dto.getTotalStock()
        );
        seckillSku.setAvailableStock(
                dto.getTotalStock()
        );
        seckillSku.setPerUserLimit(
                perUserLimit
        );

        try {
            int inserted =
                    seckillSkuMapper.insert(seckillSku);

            if (inserted != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.SECKILL_SKU_ALREADY_EXISTS,
                    exception
            );
        }

        SmsSeckillSku saved =
                seckillSkuMapper.selectById(
                        seckillSku.getId()
                );

        return SeckillSkuVO.from(saved);
    }

    @Override
    public List<SeckillSkuVO> listSkus(Long activityId) {

        SmsSeckillActivity activity =
                activityMapper.selectById(activityId);

        if (activity == null) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_NOT_FOUND
            );
        }

        return seckillSkuMapper.selectList(
                        new LambdaQueryWrapper<SmsSeckillSku>()
                                .eq(
                                        SmsSeckillSku::getActivityId,
                                        activityId
                                )
                                .orderByAsc(
                                        SmsSeckillSku::getId
                                )
                )
                .stream()
                .map(SeckillSkuVO::from)
                .toList();
    }

    @Override
    @Transactional
    public SeckillActivityVO enableActivity(
            Long activityId) {

        SmsSeckillActivity activity =
                activityMapper.selectById(activityId);

        if (activity == null) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_NOT_FOUND
            );
        }

        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Shanghai")
        );

        if (!Integer.valueOf(
                SeckillActivityStatus.DISABLED.getCode()
        ).equals(activity.getStatus())
                || !activity.getStartTime().isAfter(now)) {

            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_STATUS_INVALID
            );
        }

        List<SmsSeckillSku> seckillSkus =
                seckillSkuMapper.selectList(
                        new LambdaQueryWrapper<SmsSeckillSku>()
                                .eq(
                                        SmsSeckillSku::getActivityId,
                                        activityId
                                )
                );

        if (seckillSkus.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_EMPTY
            );
        }

        /*
         * 启用前重新检查商品是否仍然上架。
         */
        for (SmsSeckillSku seckillSku : seckillSkus) {

            PmsProduct product =
                    productMapper.selectById(
                            seckillSku.getProductId()
                    );

            if (product == null
                    || !Integer.valueOf(1)
                    .equals(product.getPublishStatus())) {

                throw new BusinessException(
                        ErrorCode.SECKILL_PRODUCT_UNAVAILABLE
                );
            }
        }

        /*
         * 条件更新保证并发启用时只有一个请求成功。
         */
        int enabled = activityMapper.update(
                null,
                new LambdaUpdateWrapper<SmsSeckillActivity>()
                        .eq(
                                SmsSeckillActivity::getId,
                                activityId
                        )
                        .eq(
                                SmsSeckillActivity::getStatus,
                                SeckillActivityStatus
                                        .DISABLED
                                        .getCode()
                        )
                        .gt(
                                SmsSeckillActivity::getStartTime,
                                now
                        )
                        .set(
                                SmsSeckillActivity::getStatus,
                                SeckillActivityStatus
                                        .ENABLED
                                        .getCode()
                        )
        );

        if (enabled != 1) {
            throw new BusinessException(
                    ErrorCode.SECKILL_ACTIVITY_STATUS_INVALID
            );
        }

        /*
         * 按SKU ID排序，降低多个活动同时锁库存时的死锁概率。
         */
        List<SmsSeckillSku> sortedSkus =
                seckillSkus.stream()
                        .sorted(
                                Comparator.comparing(
                                        SmsSeckillSku::getSkuId
                                )
                        )
                        .toList();

        /*
         * 提前锁住活动配额，使普通订单无法占用秒杀库存。
         */
        for (SmsSeckillSku seckillSku : sortedSkus) {

            int locked = skuStockMapper.lockStock(
                    seckillSku.getSkuId(),
                    seckillSku.getAvailableStock()
            );

            if (locked != 1) {
                throw new BusinessException(
                        ErrorCode.STOCK_INSUFFICIENT
                );
            }
        }

        try {
            seckillRedisService.preload(
                    activity,
                    sortedSkus
            );
        } catch (RuntimeException exception) {

            /*
             * 数据库事务会回滚状态和锁定库存，
             * 同时清理可能只写入一部分的Redis数据。
             */
            seckillRedisService.remove(sortedSkus);

            throw exception;
        }

        return getActivity(activityId);
    }
}