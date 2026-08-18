package com.mall.service;

import com.mall.domain.dto.MemberLoginDTO;
import com.mall.domain.dto.MemberRegisterDTO;
import com.mall.domain.dto.MemberUpdateDTO;
import com.mall.domain.dto.MemberUpdatePasswordDTO;
import com.mall.domain.vo.MemberInfoVO;
import com.mall.domain.vo.MemberLoginVO;

public interface UmsMemberService {

    void register(MemberRegisterDTO dto);

    MemberLoginVO login(MemberLoginDTO dto);

    MemberInfoVO updateProfile(Long memberId, MemberUpdateDTO dto);

    void updatePassword(
            Long memberId,
            MemberUpdatePasswordDTO dto
    );
}