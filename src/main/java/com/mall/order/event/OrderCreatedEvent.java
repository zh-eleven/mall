package com.mall.order.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        LocalDateTime expireTime
) {
}