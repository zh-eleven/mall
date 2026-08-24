package com.mall.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.cache.CacheNames;
import com.mall.portal.product.vo.PortalProductCategoryVO;
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

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper,
                        categoryTreeType
                );

        RedisCacheConfiguration categoryConfiguration =
                configuration
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(serializer)
                        );

        return builder -> builder.withCacheConfiguration(
                CacheNames.PORTAL_CATEGORY_TREE,
                categoryConfiguration
        );
    }
}