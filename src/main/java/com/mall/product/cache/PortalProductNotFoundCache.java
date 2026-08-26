package com.mall.product.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PortalProductNotFoundCache {

    private static final String KEY_PREFIX =
            "mall:portalProductNotFound:";

    private static final String MARKER = "1";

    private static final Duration TTL =
            Duration.ofMinutes(2);

    private final StringRedisTemplate redisTemplate;

    public boolean contains(Long productId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key(productId))
        );
    }

    public void put(Long productId) {
        redisTemplate.opsForValue().set(
                key(productId),
                MARKER,
                TTL
        );
    }

    public void evict(Long productId) {
        redisTemplate.delete(key(productId));
    }

    public void evictAfterCommit(Long productId) {
        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {
            evict(productId);
            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                evict(productId);
                            }
                        }
                );
    }

    private String key(Long productId) {
        return KEY_PREFIX + productId;
    }
}