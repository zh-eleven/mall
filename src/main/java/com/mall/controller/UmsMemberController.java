package com.mall.controller;

import com.mall.common.api.ApiResult;
import com.mall.domain.dto.MemberLoginDTO;
import com.mall.domain.dto.MemberRegisterDTO;
import com.mall.domain.entity.UmsMember;
import com.mall.domain.vo.MemberInfoVO;
import com.mall.domain.vo.MemberLoginVO;
import com.mall.security.MemberDetails;
import com.mall.service.UmsMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class UmsMemberController {

    private final UmsMemberService memberService;

    @PostMapping("/register")
    public ApiResult<Void> register(
            @Valid @RequestBody MemberRegisterDTO dto) {

        memberService.register(dto);

        return ApiResult.success(null, "注册成功");
    }

    @PostMapping("/login")
    public ApiResult<MemberLoginVO> login(
            @Valid @RequestBody MemberLoginDTO dto) {

        MemberLoginVO result = memberService.login(dto);

        return ApiResult.success(result, "登录成功");
    }

    @GetMapping("/me")
    public ApiResult<MemberInfoVO> me(
            @AuthenticationPrincipal MemberDetails memberDetails) {

        return ApiResult.success(
                MemberInfoVO.from(memberDetails.getMember())
        );
    }
}