package com.mall.seckill.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import com.mall.seckill.entity.SmsSeckillActivity;
import com.mall.seckill.entity.SmsSeckillSku;
import com.mall.seckill.enums.SeckillActivityStatus;
import com.mall.seckill.mapper.SmsSeckillActivityMapper;
import com.mall.seckill.mapper.SmsSeckillSkuMapper;
import com.mall.seckill.redis.SeckillRedisService;
import com.mall.seckill.service.impl.SeckillActivityFinalizationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillActivityFinalizationServiceImplTest {

    @Mock
    private SmsSeckillActivityMapper activityMapper;
    @Mock
    private SmsSeckillSkuMapper seckillSkuMapper;
    @Mock
    private PmsSkuStockMapper skuStockMapper;
    @Mock
    private SeckillRedisService seckillRedisService;

    @InjectMocks
    private SeckillActivityFinalizationServiceImpl service;

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                SmsSeckillActivity.class,
                SmsSeckillSku.class
        );
    }

    @BeforeEach
    void initializeSynchronization() {
        TransactionSynchronizationManager
                .initSynchronization();
    }

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager
                .isSynchronizationActive()) {
            TransactionSynchronizationManager
                    .clearSynchronization();
        }
    }

    @Test
    void shouldReleaseOnlyUnsoldQuotaAndCleanRedisAfterCommit() {
        LocalDateTime now = LocalDateTime.now();

        SmsSeckillActivity activity =
                new SmsSeckillActivity();
        activity.setId(10L);
        activity.setStatus(
                SeckillActivityStatus.ENABLED.getCode()
        );
        activity.setEndTime(now.minusMinutes(10));

        SmsSeckillSku unsold = new SmsSeckillSku();
        unsold.setId(20L);
        unsold.setActivityId(10L);
        unsold.setSkuId(30L);
        unsold.setAvailableStock(3);

        SmsSeckillSku soldOut = new SmsSeckillSku();
        soldOut.setId(21L);
        soldOut.setActivityId(10L);
        soldOut.setSkuId(31L);
        soldOut.setAvailableStock(0);

        when(activityMapper.selectByIdForUpdate(10L))
                .thenReturn(activity);
        when(seckillSkuMapper.selectList(
                any(Wrapper.class)
        )).thenReturn(List.of(unsold, soldOut));
        when(skuStockMapper.releaseLockedStock(30L, 3))
                .thenReturn(1);
        when(activityMapper.update(eq(null), any()))
                .thenReturn(1);

        assertTrue(service.finalizeExpiredActivity(
                10L,
                now
        ));

        verify(skuStockMapper)
                .releaseLockedStock(30L, 3);
        verify(skuStockMapper, never())
                .releaseLockedStock(eq(31L), any());
        verify(seckillRedisService, never())
                .remove(any());

        for (TransactionSynchronization synchronization
                : TransactionSynchronizationManager
                .getSynchronizations()) {
            synchronization.afterCommit();
        }

        verify(seckillRedisService)
                .remove(List.of(unsold, soldOut));
    }
}
