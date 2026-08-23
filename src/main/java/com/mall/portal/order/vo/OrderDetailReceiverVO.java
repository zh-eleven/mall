package com.mall.portal.order.vo;

import com.mall.order.entity.OmsOrder;

public record OrderDetailReceiverVO(
        String name,
        String phoneNumber,
        String postCode,
        String province,
        String city,
        String region,
        String detailAddress
) {

    public static OrderDetailReceiverVO from(
            OmsOrder order) {

        return new OrderDetailReceiverVO(
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverPostCode(),
                order.getReceiverProvince(),
                order.getReceiverCity(),
                order.getReceiverRegion(),
                order.getReceiverDetailAddress()
        );
    }
}