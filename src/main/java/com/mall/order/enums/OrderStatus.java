package com.mall.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum OrderStatus {

    PENDING_PAYMENT(0, "待支付"),
    PENDING_SHIPMENT(1, "待发货"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELED(4, "已取消");

    /**
     * 写入数据库的值。
     */
    @EnumValue
    private final int code;

    private final String description;

    OrderStatus(int code, String description) {
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