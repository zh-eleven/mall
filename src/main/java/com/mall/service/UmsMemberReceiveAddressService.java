package com.mall.service;

import com.mall.domain.dto.MemberAddressCreateDTO;
import com.mall.domain.dto.MemberAddressUpdateDTO;
import com.mall.domain.vo.MemberAddressVO;

import java.util.List;

public interface UmsMemberReceiveAddressService {

    MemberAddressVO create(
            Long memberId,
            MemberAddressCreateDTO dto
    );

    List<MemberAddressVO> list(Long memberId);

    MemberAddressVO update(
            Long memberId,
            Long addressId,
            MemberAddressUpdateDTO dto
    );

    void delete(
            Long memberId,
            Long addressId
    );

    void setDefault(
            Long memberId,
            Long addressId
    );
}