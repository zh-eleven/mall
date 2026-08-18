package com.mall.member.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberLoginVO {

    private Long userId;

    private String username;

    private String token;
}