package com.mall.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.cache.CacheNames;
import com.mall.portal.product.vo.PortalProductCategoryVO;
import com.mall.portal.product.vo.PortalProductDetailCacheVO;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .prefixCacheNameWith("mall:");
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer
    redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration configuration,
            ObjectMapper objectMapper) {

        JavaType categoryTreeType =
                objectMapper.getTypeFactory()
                        .constructCollectionType(
                                List.class,
                                PortalProductCategoryVO.class
                        );

        Jackson2JsonRedisSerializer<Object> categorySerializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper,
                        categoryTreeType
                );

        Jackson2JsonRedisSerializer<PortalProductDetailCacheVO>
                productDetailSerializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper,
                        PortalProductDetailCacheVO.class
                );

        RedisCacheConfiguration categoryConfiguration =
                configuration
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(categorySerializer)
                        );

        RedisCacheConfiguration productDetailConfiguration =
                configuration
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                productDetailSerializer
                                        )
                        );

        return builder -> builder
                .transactionAware()
                .withCacheConfiguration(
                        CacheNames.PORTAL_CATEGORY_TREE,
                        categoryConfiguration
                )
                .withCacheConfiguration(
                        CacheNames.PORTAL_PRODUCT_DETAIL,
                        productDetailConfiguration
                );
    }
}
