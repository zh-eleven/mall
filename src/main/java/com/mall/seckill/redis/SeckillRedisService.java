package com.mall.seckill.redis;

import com.mall.seckill.entity.SmsSeckillActivity;
import com.mall.seckill.entity.SmsSeckillSku;
import com.mall.seckill.enums.SeckillReserveResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeckillRedisService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Shanghai");

    private static final Duration EXPIRE_BUFFER =
            Duration.ofHours(1);

    private static final DefaultRedisScript<Long>
            RESERVE_SCRIPT;

    private static final DefaultRedisScript<Long>
            ROLLBACK_SCRIPT;

    static {
        RESERVE_SCRIPT = new DefaultRedisScript<>();

        RESERVE_SCRIPT.setLocation(
                new ClassPathResource(
                        "redis/seckill-reserve.lua"
                )
        );

        RESERVE_SCRIPT.setResultType(Long.class);

        ROLLBACK_SCRIPT = new DefaultRedisScript<>();

        ROLLBACK_SCRIPT.setLocation(
                new ClassPathResource(
                        "redis/seckill-rollback.lua"
                )
        );

        ROLLBACK_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;

    public void preload(
            SmsSeckillActivity activity,
            List<SmsSeckillSku> seckillSkus) {

        Duration ttl = Duration.between(
                LocalDateTime.now(BUSINESS_ZONE),
                activity.getEndTime()
        ).plus(EXPIRE_BUFFER);

        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException(
                    "秒杀活动已经结束"
            );
        }

        long startTimestamp =
                activity.getStartTime()
                        .atZone(BUSINESS_ZONE)
                        .toInstant()
                        .toEpochMilli();

        long endTimestamp =
                activity.getEndTime()
                        .atZone(BUSINESS_ZONE)
                        .toInstant()
                        .toEpochMilli();

        for (SmsSeckillSku seckillSku : seckillSkus) {

            String skuKey =
                    SeckillRedisKeys.sku(
                            seckillSku.getId()
                    );

            String usersKey =
                    SeckillRedisKeys.users(
                            seckillSku.getId()
                    );

            String reservationsKey =
                    SeckillRedisKeys.reservations(
                            seckillSku.getId()
                    );

            String pendingKey =
                    SeckillRedisKeys.pending(
                            seckillSku.getId()
                    );

            /*
             * 清理之前可能预热失败留下的数据。
             */
            redisTemplate.delete(
                    List.of(
                            skuKey,
                            usersKey,
                            reservationsKey,
                            pendingKey
                    )
            );

            Map<String, String> data =
                    new HashMap<>();

            data.put(
                    "activityId",
                    activity.getId().toString()
            );

            data.put(
                    "productId",
                    seckillSku.getProductId().toString()
            );

            data.put(
                    "skuId",
                    seckillSku.getSkuId().toString()
            );

            data.put(
                    "price",
                    seckillSku.getSeckillPrice()
                            .toPlainString()
            );

            data.put(
                    "stock",
                    seckillSku.getAvailableStock()
                            .toString()
            );

            data.put(
                    "perUserLimit",
                    seckillSku.getPerUserLimit()
                            .toString()
            );

            data.put(
                    "startTime",
                    Long.toString(startTimestamp)
            );

            data.put(
                    "endTime",
                    Long.toString(endTimestamp)
            );

            redisTemplate.opsForHash()
                    .putAll(skuKey, data);

            redisTemplate.expire(skuKey, ttl);
        }
    }

    public void remove(
            List<SmsSeckillSku> seckillSkus) {

        List<String> keys =
                new ArrayList<>(
                        seckillSkus.size() * 4
                );

        for (SmsSeckillSku seckillSku : seckillSkus) {

            Long seckillSkuId =
                    seckillSku.getId();

            keys.add(
                    SeckillRedisKeys.sku(
                            seckillSkuId
                    )
            );

            keys.add(
                    SeckillRedisKeys.users(
                            seckillSkuId
                    )
            );

            keys.add(
                    SeckillRedisKeys.reservations(
                            seckillSkuId
                    )
            );

            keys.add(
                    SeckillRedisKeys.pending(
                            seckillSkuId
                    )
            );
        }

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public SeckillReserveResult reserve(
            Long seckillSkuId,
            String requestId,
            Long memberId,
            Long addressId,
            int quantity) {

        Long result = redisTemplate.execute(
                RESERVE_SCRIPT,
                List.of(
                        SeckillRedisKeys.sku(
                                seckillSkuId
                        ),
                        SeckillRedisKeys.users(
                                seckillSkuId
                        ),
                        SeckillRedisKeys.reservations(
                                seckillSkuId
                        ),
                        SeckillRedisKeys.pending(
                                seckillSkuId
                        )
                ),
                requestId,
                memberId.toString(),
                Integer.toString(quantity),
                addressId.toString()
        );

        return SeckillReserveResult.fromCode(
                result
        );
    }

    public boolean rollbackReservation(
            Long seckillSkuId,
            String requestId) {

        Long result = redisTemplate.execute(
                ROLLBACK_SCRIPT,
                List.of(
                        SeckillRedisKeys.sku(
                                seckillSkuId
                        ),
                        SeckillRedisKeys.users(
                                seckillSkuId
                        ),
                        SeckillRedisKeys.reservations(
                                seckillSkuId
                        ),
                        SeckillRedisKeys.pending(
                                seckillSkuId
                        )
                ),
                requestId
        );

        return result != null
                && (result == 0L
                || result == 1L);
    }

    public boolean hasReservation(
            Long seckillSkuId,
            String requestId,
            Long memberId) {

        Object reservation =
                redisTemplate.opsForHash()
                        .get(
                                SeckillRedisKeys.reservations(
                                        seckillSkuId
                                ),
                                requestId
                        );

        if (reservation == null) {
            return false;
        }

        return reservation.toString()
                .startsWith(memberId + ":1:");
    }

    public void markCompleted(
            Long seckillSkuId,
            String requestId) {

        redisTemplate.opsForZSet().remove(
                SeckillRedisKeys.pending(seckillSkuId),
                requestId
        );
    }

    public void touchPending(
            Long seckillSkuId,
            String requestId,
            long timestamp) {

        redisTemplate.opsForZSet().add(
                SeckillRedisKeys.pending(seckillSkuId),
                requestId,
                timestamp
        );
    }

    public List<SeckillPendingReservation>
    findPendingBefore(
            Long seckillSkuId,
            long beforeTimestamp,
            int limit) {

        Set<String> requestIds =
                redisTemplate.opsForZSet()
                        .rangeByScore(
                                SeckillRedisKeys.pending(
                                        seckillSkuId
                                ),
                                0,
                                beforeTimestamp,
                                0,
                                limit
                        );

        if (requestIds == null || requestIds.isEmpty()) {
            return List.of();
        }

        List<SeckillPendingReservation> result =
                new ArrayList<>(requestIds.size());

        for (String requestId : requestIds) {
            Object raw = redisTemplate.opsForHash().get(
                    SeckillRedisKeys.reservations(
                            seckillSkuId
                    ),
                    requestId
            );

            if (raw == null) {
                markCompleted(seckillSkuId, requestId);
                continue;
            }

            SeckillPendingReservation parsed =
                    parseReservation(
                            seckillSkuId,
                            requestId,
                            raw.toString()
                    );

            if (parsed != null) {
                result.add(parsed);
            }
        }

        return result;
    }

    private SeckillPendingReservation parseReservation(
            Long seckillSkuId,
            String requestId,
            String raw) {

        String[] fields = raw.split(":", 4);

        if (fields.length != 4) {
            return null;
        }

        try {
            return new SeckillPendingReservation(
                    requestId,
                    seckillSkuId,
                    Long.valueOf(fields[0]),
                    Long.valueOf(fields[2]),
                    Integer.valueOf(fields[1]),
                    Long.valueOf(fields[3])
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
