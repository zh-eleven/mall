package com.mall.member.service;

import com.mall.common.api.PageResult;
import com.mall.member.dto.AdminMemberQueryDTO;
import com.mall.member.dto.MemberStatusUpdateDTO;
import com.mall.member.vo.AdminMemberVO;

public interface AdminMemberService {

    PageResult<AdminMemberVO> page(AdminMemberQueryDTO query);

    AdminMemberVO getDetail(Long memberId);

    AdminMemberVO updateStatus(
            Long memberId,
            MemberStatusUpdateDTO dto
    );
}
