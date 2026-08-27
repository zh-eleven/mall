package com.mall.seckill.enums;

import lombok.Getter;

@Getter
public enum SeckillActivityStatus {

    DISABLED(0),
    ENABLED(1),
    ENDED(2);

    private final int code;

    SeckillActivityStatus(int code) {
        this.code = code;
    }
}
