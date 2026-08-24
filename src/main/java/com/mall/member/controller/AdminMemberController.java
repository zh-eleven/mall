package com.mall.member.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.member.dto.AdminMemberQueryDTO;
import com.mall.member.dto.MemberStatusUpdateDTO;
import com.mall.member.service.AdminMemberService;
import com.mall.member.vo.AdminMemberVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Validated
public class AdminMemberController {

    private final AdminMemberService memberService;

    @GetMapping
    @PreAuthorize("hasAuthority('member:read')")
    public ApiResult<PageResult<AdminMemberVO>> page(
            @Valid AdminMemberQueryDTO query) {
        return ApiResult.success(memberService.page(query));
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasAuthority('member:read')")
    public ApiResult<AdminMemberVO> detail(
            @PathVariable
            @Positive(message = "会员ID必须大于0")
            Long memberId) {
        return ApiResult.success(
                memberService.getDetail(memberId)
        );
    }

    @PatchMapping("/{memberId}/status")
    @PreAuthorize("hasAuthority('member:write')")
    public ApiResult<AdminMemberVO> updateStatus(
            @PathVariable
            @Positive(message = "会员ID必须大于0")
            Long memberId,
            @Valid @RequestBody MemberStatusUpdateDTO dto) {
        return ApiResult.success(
                memberService.updateStatus(memberId, dto),
                "会员状态修改成功"
        );
    }
}
