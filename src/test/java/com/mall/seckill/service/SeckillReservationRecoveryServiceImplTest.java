package com.mall.seckill.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.seckill.entity.SmsSeckillActivity;
import com.mall.seckill.entity.SmsSeckillSku;
import com.mall.seckill.entity.OmsSeckillFailure;
import com.mall.seckill.entity.OmsSeckillOrder;
import com.mall.product.service.MybatisTestSupport;
import com.mall.seckill.mapper.OmsSeckillFailureMapper;
import com.mall.seckill.mapper.OmsSeckillOrderMapper;
import com.mall.seckill.mapper.SmsSeckillActivityMapper;
import com.mall.seckill.mapper.SmsSeckillSkuMapper;
import com.mall.seckill.mq.SeckillOrderMessage;
import com.mall.seckill.mq.SeckillOrderMessagePublisher;
import com.mall.seckill.redis.SeckillPendingReservation;
import com.mall.seckill.redis.SeckillRedisService;
import com.mall.seckill.service.impl.SeckillReservationRecoveryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillReservationRecoveryServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                SmsSeckillActivity.class,
                SmsSeckillSku.class,
                OmsSeckillOrder.class,
                OmsSeckillFailure.class
        );
    }

    @Mock
    private SmsSeckillActivityMapper activityMapper;
    @Mock
    private SmsSeckillSkuMapper seckillSkuMapper;
    @Mock
    private OmsSeckillOrderMapper seckillOrderMapper;
    @Mock
    private OmsSeckillFailureMapper failureMapper;
    @Mock
    private SeckillRedisService seckillRedisService;
    @Mock
    private SeckillOrderMessagePublisher messagePublisher;

    @InjectMocks
    private SeckillReservationRecoveryServiceImpl service;

    @Test
    void shouldRepublishStaleReservationWithoutTerminalRecord() {
        SmsSeckillActivity activity =
                new SmsSeckillActivity();
        activity.setId(10L);

        SmsSeckillSku sku = new SmsSeckillSku();
        sku.setId(20L);
        sku.setActivityId(10L);

        when(activityMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(activity));

        Page<SmsSeckillSku> page = new Page<>();
        page.setRecords(List.of(sku));

        when(seckillSkuMapper.selectPage(
                any(Page.class),
                any(Wrapper.class)
        )).thenReturn(page);

        when(seckillRedisService.findPendingBefore(
                eq(20L),
                anyLong(),
                eq(100)
        )).thenReturn(List.of(
                new SeckillPendingReservation(
                        "request-1",
                        20L,
                        30L,
                        40L,
                        1,
                        1000L
                )
        ));

        when(seckillOrderMapper.selectCount(any(Wrapper.class)))
                .thenReturn(0L);
        when(failureMapper.selectCount(any(Wrapper.class)))
                .thenReturn(0L);

        int recovered = service.recoverStaleReservations(
                2000L,
                1000,
                100
        );

        assertEquals(1, recovered);

        verify(seckillRedisService).touchPending(
                eq(20L),
                eq("request-1"),
                anyLong()
        );

        ArgumentCaptor<SeckillOrderMessage> captor =
                ArgumentCaptor.forClass(
                        SeckillOrderMessage.class
                );
        verify(messagePublisher).publish(captor.capture());

        SeckillOrderMessage message = captor.getValue();
        assertEquals("request-1", message.requestId());
        assertEquals(30L, message.memberId());
        assertEquals(40L, message.addressId());
        assertEquals(1000L, message.requestedAt());
    }
}
