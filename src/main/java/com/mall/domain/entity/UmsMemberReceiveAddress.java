package com.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ums_member_receive_address")
public class UmsMemberReceiveAddress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private String name;

    private String phoneNumber;

    /**
     * 0：普通地址
     * 1：默认地址
     */
    private Integer defaultStatus;

    private String postCode;

    private String province;

    private String city;

    private String region;

    private String detailAddress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}