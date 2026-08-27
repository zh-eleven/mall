package com.mall.seckill;

import com.mall.seckill.mq.SeckillRabbitConfig;
import com.mall.seckill.redis.SeckillRedisKeys;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeckillInfrastructureTest {

    @Test
    void luaScriptsShouldBePackagedAsClasspathResources()
            throws IOException {

        for (String path : new String[]{
                "redis/seckill-reserve.lua",
                "redis/seckill-rollback.lua"
        }) {
            ClassPathResource resource =
                    new ClassPathResource(path);

            assertTrue(resource.exists(), path);
            assertTrue(resource.contentLength() > 0, path);
        }
    }

    @Test
    void deadQueueShouldBeBoundToDeadExchange() {
        SeckillRabbitConfig config =
                new SeckillRabbitConfig();

        var exchange = config.seckillDeadExchange();
        var queue = config.seckillDeadQueue();
        var binding = config.seckillDeadBinding(
                queue,
                exchange
        );

        assertEquals(
                SeckillRabbitConfig.DEAD_EXCHANGE,
                binding.getExchange()
        );
        assertEquals(
                SeckillRabbitConfig.DEAD_QUEUE,
                binding.getDestination()
        );
        assertEquals(
                SeckillRabbitConfig.DEAD_ROUTING_KEY,
                binding.getRoutingKey()
        );
    }

    @Test
    void allLuaKeysShouldUseSameRedisClusterSlot() {
        Long skuId = 42L;

        for (String key : new String[]{
                SeckillRedisKeys.sku(skuId),
                SeckillRedisKeys.users(skuId),
                SeckillRedisKeys.reservations(skuId),
                SeckillRedisKeys.pending(skuId)
        }) {
            assertTrue(key.contains("{42}"), key);
        }
    }
}
