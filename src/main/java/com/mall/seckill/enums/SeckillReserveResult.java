package com.mall.seckill.enums;

import lombok.Getter;

@Getter
public enum SeckillReserveResult {

    SUCCESS(0),
    NOT_STARTED(1),
    ENDED(2),
    STOCK_INSUFFICIENT(3),
    LIMIT_EXCEEDED(4),
    DATA_MISSING(5),
    DUPLICATE_REQUEST(6);

    private final long code;

    SeckillReserveResult(long code) {
        this.code = code;
    }

    public static SeckillReserveResult fromCode(
            Long code) {

        if (code != null) {
            for (SeckillReserveResult result : values()) {
                if (result.code == code) {
                    return result;
                }
            }
        }

        return DATA_MISSING;
    }
}