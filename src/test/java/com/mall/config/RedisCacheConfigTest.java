package com.mall.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.cache.CacheNames;
import com.mall.portal.product.vo.PortalProductAttributeVO;
import com.mall.portal.product.vo.PortalProductCategoryVO;
import com.mall.portal.product.vo.PortalProductDetailCacheVO;
import com.mall.portal.product.vo.PortalSkuCacheVO;
import org.junit.jupiter.api.Test;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisCacheConfigTest {

    private final RedisCacheConfig config = new RedisCacheConfig();

    @Test
    void defaultConfigurationShouldUseExpectedTtlPrefixAndNullPolicy() {
        RedisCacheConfiguration configuration =
                config.redisCacheConfiguration();

        assertEquals(Duration.ofMinutes(10), configuration.getTtl());
        assertEquals(
                "mall:sample::",
                configuration.getKeyPrefixFor("sample")
        );
        assertFalse(configuration.getAllowCacheNullValues());
    }

    @Test
    void customizerShouldConfigureTypedCachesAndBoundedJitter() {
        RedisCacheConfiguration defaults =
                config.redisCacheConfiguration();
        RedisCacheManager manager = cacheManager(defaults);

        RedisCacheConfiguration categoryConfiguration =
                cacheConfiguration(
                        manager,
                        CacheNames.PORTAL_CATEGORY_TREE
                );
        RedisCacheConfiguration detailConfiguration =
                cacheConfiguration(
                        manager,
                        CacheNames.PORTAL_PRODUCT_DETAIL
                );

        assertEquals(
                Duration.ofMinutes(30),
                categoryConfiguration.getTtl()
        );
        assertTrue(manager.isTransactionAware());

        for (int index = 0; index < 100; index++) {
            Duration ttl = detailConfiguration
                    .getTtlFunction()
                    .getTimeToLive("10", new byte[0]);

            assertFalse(ttl.compareTo(Duration.ofMinutes(8)) < 0);
            assertFalse(ttl.compareTo(Duration.ofMinutes(12)) > 0);
        }
    }

    @Test
    void categoryCacheSerializerShouldRoundTripConcreteRecordTypes() {
        RedisCacheManager manager = cacheManager(
                config.redisCacheConfiguration()
        );
        RedisCacheConfiguration configuration =
                cacheConfiguration(
                        manager,
                        CacheNames.PORTAL_CATEGORY_TREE
                );
        List<PortalProductCategoryVO> expected = List.of(
                new PortalProductCategoryVO(
                        1L,
                        "手机",
                        "phone.png",
                        List.of(new PortalProductCategoryVO(
                                2L,
                                "智能手机",
                                null,
                                List.of()
                        ))
                )
        );

        ByteBuffer serialized = configuration
                .getValueSerializationPair()
                .write(expected);
        Object actual = configuration
                .getValueSerializationPair()
                .read(serialized);

        assertEquals(expected, actual);
        assertInstanceOf(
                PortalProductCategoryVO.class,
                ((List<?>) actual).getFirst()
        );
    }

    @Test
    void productDetailSerializerShouldRoundTripDetailRecord() {
        RedisCacheManager manager = cacheManager(
                config.redisCacheConfiguration()
        );
        RedisCacheConfiguration configuration =
                cacheConfiguration(
                        manager,
                        CacheNames.PORTAL_PRODUCT_DETAIL
                );
        PortalProductDetailCacheVO expected = detail();

        ByteBuffer serialized = configuration
                .getValueSerializationPair()
                .write(expected);
        Object actual = configuration
                .getValueSerializationPair()
                .read(serialized);

        assertEquals(expected, actual);
        assertInstanceOf(PortalProductDetailCacheVO.class, actual);
    }

    private RedisCacheManager cacheManager(
            RedisCacheConfiguration defaults) {
        RedisCacheWriter writer = mock(RedisCacheWriter.class);
        when(writer.withStatisticsCollector(any()))
                .thenReturn(writer);

        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(writer)
                        .cacheDefaults(defaults);

        config.redisCacheManagerBuilderCustomizer(
                defaults,
                new ObjectMapper()
        ).customize(builder);

        RedisCacheManager manager = builder.build();
        verify(writer).withStatisticsCollector(any());
        manager.afterPropertiesSet();
        return manager;
    }

    private RedisCacheConfiguration cacheConfiguration(
            RedisCacheManager manager,
            String cacheName) {
        TransactionAwareCacheDecorator decorated = assertInstanceOf(
                TransactionAwareCacheDecorator.class,
                manager.getCache(cacheName)
        );
        RedisCache redisCache = assertInstanceOf(
                RedisCache.class,
                decorated.getTargetCache()
        );
        return redisCache.getCacheConfiguration();
    }

    private PortalProductDetailCacheVO detail() {
        return new PortalProductDetailCacheVO(
                10L,
                20L,
                30L,
                "测试手机",
                "新品",
                new BigDecimal("1999.00"),
                new BigDecimal("2299.00"),
                "台",
                "main.png",
                List.of("album-1.png", "album-2.png"),
                "描述",
                "详情标题",
                "详情摘要",
                "<p>详情</p>",
                List.of(new PortalProductAttributeVO(
                        40L,
                        "颜色",
                        "黑色"
                )),
                List.of(new PortalSkuCacheVO(
                        50L,
                        new BigDecimal("1999.00"),
                        "sku.png",
                        "{\"颜色\":\"黑色\"}"
                ))
        );
    }
}
