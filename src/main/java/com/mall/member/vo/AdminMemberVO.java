package com.mall.member.vo;

import com.mall.member.entity.UmsMember;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminMemberVO(
        Long id,
        String username,
        String nickname,
        String phone,
        String email,
        String avatar,
        Integer gender,
        LocalDate birthday,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static AdminMemberVO from(UmsMember member) {
        return new AdminMemberVO(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getPhone(),
                member.getEmail(),
                member.getAvatar(),
                member.getGender(),
                member.getBirthday(),
                member.getStatus(),
                member.getCreateTime(),
                member.getUpdateTime()
        );
    }
}
