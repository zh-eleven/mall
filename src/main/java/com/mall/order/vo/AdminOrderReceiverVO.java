package com.mall.order.vo;

import com.mall.order.entity.OmsOrder;

public record AdminOrderReceiverVO(
        String name,
        String phone,
        String postCode,
        String province,
        String city,
        String region,
        String detailAddress
) {

    public static AdminOrderReceiverVO from(
            OmsOrder order) {

        return new AdminOrderReceiverVO(
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
