package com.mall.domain.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberAddressUpdateDTO {

    @Pattern(regexp = ".*\\S.*", message = "收货人不能为空")
    @Size(max = 100)
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "手机号不能为空")
    @Size(max = 32)
    private String phoneNumber;

    @Size(max = 16)
    private String postCode;

    @Pattern(regexp = ".*\\S.*", message = "省份不能为空")
    @Size(max = 64)
    private String province;

    @Pattern(regexp = ".*\\S.*", message = "城市不能为空")
    @Size(max = 64)
    private String city;

    @Pattern(regexp = ".*\\S.*", message = "区域不能为空")
    @Size(max = 64)
    private String region;

    @Pattern(regexp = ".*\\S.*", message = "详细地址不能为空")
    @Size(max = 255)
    private String detailAddress;
}