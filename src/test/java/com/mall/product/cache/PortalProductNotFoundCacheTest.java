package com.mall.product.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalProductNotFoundCacheTest {

    private static final Long PRODUCT_ID = 10L;
    private static final String KEY =
            "mall:portalProductNotFound:10";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager
                .isSynchronizationActive()) {
            TransactionSynchronizationManager
                    .clearSynchronization();
        }
    }

    @Test
    void containsShouldOnlyReturnTrueForExistingMarker() {
        PortalProductNotFoundCache cache = cache();

        when(redisTemplate.hasKey(KEY))
                .thenReturn(true, false, null);

        assertTrue(cache.contains(PRODUCT_ID));
        assertFalse(cache.contains(PRODUCT_ID));
        assertFalse(cache.contains(PRODUCT_ID));
    }

    @Test
    void putShouldStoreMarkerWithTwoMinuteTtl() {
        PortalProductNotFoundCache cache = cache();

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        cache.put(PRODUCT_ID);

        verify(valueOperations).set(
                KEY,
                "1",
                Duration.ofMinutes(2)
        );
    }

    @Test
    void evictAfterCommitShouldDeleteImmediatelyWithoutTransaction() {
        PortalProductNotFoundCache cache = cache();

        cache.evictAfterCommit(PRODUCT_ID);

        verify(redisTemplate).delete(KEY);
    }

    @Test
    void evictAfterCommitShouldWaitUntilTransactionCommits() {
        PortalProductNotFoundCache cache = cache();
        TransactionSynchronizationManager.initSynchronization();

        cache.evictAfterCommit(PRODUCT_ID);

        verify(redisTemplate, never()).delete(KEY);

        TransactionSynchronizationUtils.triggerAfterCommit();

        verify(redisTemplate).delete(KEY);
    }

    @Test
    void evictAfterCommitShouldKeepMarkerWhenTransactionRollsBack() {
        PortalProductNotFoundCache cache = cache();
        TransactionSynchronizationManager.initSynchronization();

        cache.evictAfterCommit(PRODUCT_ID);
        TransactionSynchronizationUtils.triggerAfterCompletion(
                TransactionSynchronization.STATUS_ROLLED_BACK
        );

        verify(redisTemplate, never()).delete(KEY);
    }

    private PortalProductNotFoundCache cache() {
        return new PortalProductNotFoundCache(redisTemplate);
    }
}
