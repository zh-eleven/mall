package com.mall.member.controller;

import com.mall.common.api.ApiResult;
import com.mall.member.dto.MemberAddressCreateDTO;
import com.mall.member.dto.MemberAddressUpdateDTO;
import com.mall.member.vo.MemberAddressVO;
import com.mall.security.MemberDetails;
import com.mall.member.service.UmsMemberReceiveAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/me/addresses")
@RequiredArgsConstructor
public class UmsMemberReceiveAddressController {

    private final UmsMemberReceiveAddressService addressService;

    @PostMapping
    public ApiResult<MemberAddressVO> create(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Valid @RequestBody MemberAddressCreateDTO dto) {

        Long memberId = memberDetails.getMember().getId();

        MemberAddressVO result =
                addressService.create(memberId, dto);

        return ApiResult.success(
                result,
                "收货地址添加成功"
        );
    }

    @GetMapping
    public ApiResult<List<MemberAddressVO>> list(
            @AuthenticationPrincipal MemberDetails memberDetails) {

        Long memberId = memberDetails.getMember().getId();

        return ApiResult.success(
                addressService.list(memberId)
        );
    }

    @PatchMapping("/{addressId}")
    public ApiResult<MemberAddressVO> update(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long addressId,
            @Valid @RequestBody MemberAddressUpdateDTO dto) {

        Long memberId = memberDetails.getMember().getId();

        MemberAddressVO result =
                addressService.update(
                        memberId,
                        addressId,
                        dto
                );

        return ApiResult.success(
                result,
                "收货地址修改成功"
        );
    }

    @DeleteMapping("/{addressId}")
    public ApiResult<Void> delete(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long addressId) {

        Long memberId = memberDetails.getMember().getId();

        addressService.delete(memberId, addressId);

        return ApiResult.success(
                null,
                "收货地址删除成功"
        );
    }

    @PatchMapping("/{addressId}/default")
    public ApiResult<Void> setDefault(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long addressId) {

        Long memberId = memberDetails.getMember().getId();

        addressService.setDefault(memberId, addressId);

        return ApiResult.success(
                null,
                "默认地址设置成功"
        );
    }
}