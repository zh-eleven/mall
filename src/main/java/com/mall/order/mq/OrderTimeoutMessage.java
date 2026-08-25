package com.mall.order.mq;

import java.time.LocalDateTime;

public record OrderTimeoutMessage(
        Long orderId,
        LocalDateTime expireTime
) {
}