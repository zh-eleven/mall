package com.mall.domain.vo;

import com.mall.domain.entity.UmsMember;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;

    public static MemberInfoVO from(UmsMember member) {
        MemberInfoVO vo = new MemberInfoVO();

        vo.setId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setNickname(member.getNickname());
        vo.setPhone(member.getPhone());
        vo.setEmail(member.getEmail());
        vo.setAvatar(member.getAvatar());
        vo.setGender(member.getGender());
        vo.setBirthday(member.getBirthday());

        return vo;
    }
}