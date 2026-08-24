package com.mall.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum RefundStatus {

    APPLYING(0, "待审核"),
    COMPLETED(1, "已退款"),
    REJECTED(2, "已拒绝");

    @EnumValue
    private final int code;

    private final String description;

    RefundStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
