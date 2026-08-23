package com.mall.portal.order.vo;

import com.mall.member.entity.UmsMemberReceiveAddress;

public record OrderReceiverVO(
        Long addressId,
        String name,
        String phoneNumber,
        String postCode,
        String province,
        String city,
        String region,
        String detailAddress
) {

    public static OrderReceiverVO from(
            UmsMemberReceiveAddress address) {

        return new OrderReceiverVO(
                address.getId(),
                address.getName(),
                address.getPhoneNumber(),
                address.getPostCode(),
                address.getProvince(),
                address.getCity(),
                address.getRegion(),
                address.getDetailAddress()
        );
    }
}