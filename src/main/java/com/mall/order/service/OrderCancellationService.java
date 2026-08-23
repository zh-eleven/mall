package com.mall.order.service;

import java.time.LocalDateTime;

public interface OrderCancellationService {

    void cancelByMember(
            Long memberId,
            Long orderId
    );

    boolean cancelTimedOutOrder(
            Long orderId,
            LocalDateTime cutoffTime
    );
}
