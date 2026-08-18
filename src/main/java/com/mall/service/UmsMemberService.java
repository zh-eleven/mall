package com.mall.service;

import com.mall.domain.dto.MemberLoginDTO;
import com.mall.domain.dto.MemberRegisterDTO;
import com.mall.domain.vo.MemberLoginVO;

public interface UmsMemberService {

    void register(MemberRegisterDTO dto);

    MemberLoginVO login(MemberLoginDTO dto);
}