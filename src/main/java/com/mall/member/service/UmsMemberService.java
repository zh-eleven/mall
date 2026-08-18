package com.mall.member.service;

import com.mall.member.dto.MemberLoginDTO;
import com.mall.member.dto.MemberRegisterDTO;
import com.mall.member.dto.MemberUpdateDTO;
import com.mall.member.dto.MemberUpdatePasswordDTO;
import com.mall.member.vo.MemberInfoVO;
import com.mall.member.vo.MemberLoginVO;

public interface UmsMemberService {

    void register(MemberRegisterDTO dto);

    MemberLoginVO login(MemberLoginDTO dto);

    MemberInfoVO updateProfile(Long memberId, MemberUpdateDTO dto);

    void updatePassword(
            Long memberId,
            MemberUpdatePasswordDTO dto
    );
}