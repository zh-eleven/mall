package com.mall.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberAddressCreateDTO {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 100, message = "收货人不能超过100个字符")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Size(max = 32, message = "手机号不能超过32个字符")
    private String phoneNumber;

    @Min(value = 0, message = "默认状态只能是0或1")
    @Max(value = 1, message = "默认状态只能是0或1")
    private Integer defaultStatus;

    @Size(max = 16, message = "邮政编码不能超过16个字符")
    private String postCode;

    @NotBlank(message = "省份不能为空")
    @Size(max = 64)
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 64)
    private String city;

    @NotBlank(message = "区域不能为空")
    @Size(max = 64)
    private String region;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255)
    private String detailAddress;
}