package com.mall.domain.vo;

import com.mall.domain.entity.UmsMemberReceiveAddress;
import lombok.Data;

@Data
public class MemberAddressVO {

    private Long id;

    private String name;

    private String phoneNumber;

    private Integer defaultStatus;

    private String postCode;

    private String province;

    private String city;

    private String region;

    private String detailAddress;

    public static MemberAddressVO from(
            UmsMemberReceiveAddress address) {

        MemberAddressVO vo = new MemberAddressVO();

        vo.setId(address.getId());
        vo.setName(address.getName());
        vo.setPhoneNumber(address.getPhoneNumber());
        vo.setDefaultStatus(address.getDefaultStatus());
        vo.setPostCode(address.getPostCode());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setRegion(address.getRegion());
        vo.setDetailAddress(address.getDetailAddress());

        return vo;
    }
}