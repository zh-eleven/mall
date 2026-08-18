package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.domain.dto.MemberAddressCreateDTO;
import com.mall.domain.dto.MemberAddressUpdateDTO;
import com.mall.domain.entity.UmsMemberReceiveAddress;
import com.mall.domain.vo.MemberAddressVO;
import com.mall.mapper.UmsMemberReceiveAddressMapper;
import com.mall.service.UmsMemberReceiveAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UmsMemberReceiveAddressServiceImpl
        implements UmsMemberReceiveAddressService {

    private final UmsMemberReceiveAddressMapper addressMapper;

    @Override
    @Transactional
    public MemberAddressVO create(
            Long memberId,
            MemberAddressCreateDTO dto) {

        boolean makeDefault =
                Integer.valueOf(1).equals(dto.getDefaultStatus());

        // 新地址设为默认时，先取消原默认地址
        if (makeDefault) {
            clearDefault(memberId);
        }

        UmsMemberReceiveAddress address =
                new UmsMemberReceiveAddress();

        address.setMemberId(memberId);
        address.setName(dto.getName().trim());
        address.setPhoneNumber(dto.getPhoneNumber().trim());
        address.setDefaultStatus(makeDefault ? 1 : 0);
        address.setPostCode(
                normalizeOptionalText(dto.getPostCode())
        );
        address.setProvince(dto.getProvince().trim());
        address.setCity(dto.getCity().trim());
        address.setRegion(dto.getRegion().trim());
        address.setDetailAddress(
                dto.getDetailAddress().trim()
        );

        addressMapper.insert(address);

        return MemberAddressVO.from(address);
    }

    @Override
    public List<MemberAddressVO> list(Long memberId) {

        List<UmsMemberReceiveAddress> addresses =
                addressMapper.selectList(
                        new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                                .eq(
                                        UmsMemberReceiveAddress::getMemberId,
                                        memberId
                                )
                                .orderByDesc(
                                        UmsMemberReceiveAddress::getDefaultStatus
                                )
                                .orderByDesc(
                                        UmsMemberReceiveAddress::getId
                                )
                );

        return addresses.stream()
                .map(MemberAddressVO::from)
                .toList();
    }

    @Override
    public MemberAddressVO update(
            Long memberId,
            Long addressId,
            MemberAddressUpdateDTO dto) {

        UmsMemberReceiveAddress current =
                findOwnedAddress(memberId, addressId);

        LambdaUpdateWrapper<UmsMemberReceiveAddress> wrapper =
                new LambdaUpdateWrapper<UmsMemberReceiveAddress>()
                        .eq(
                                UmsMemberReceiveAddress::getId,
                                addressId
                        )
                        .eq(
                                UmsMemberReceiveAddress::getMemberId,
                                memberId
                        );

        boolean hasUpdate = false;

        if (dto.getName() != null) {
            wrapper.set(
                    UmsMemberReceiveAddress::getName,
                    dto.getName().trim()
            );
            hasUpdate = true;
        }

        if (dto.getPhoneNumber() != null) {
            wrapper.set(
                    UmsMemberReceiveAddress::getPhoneNumber,
                    dto.getPhoneNumber().trim()
            );
            hasUpdate = true;
        }

        if (dto.getPostCode() != null) {
            wrapper.set(
                    UmsMemberReceiveAddress::getPostCode,
                    normalizeOptionalText(dto.getPostCode())
            );
            hasUpdate = true;
        }

        if (dto.getProvince() != null) {
            wrapper.set(
                    UmsMemberReceiveAddress::getProvince,
                    dto.getProvince().trim()
            );
            hasUpdate = true;
        }

        if (dto.getCity() != null) {
            wrapper.set(
                    UmsMemberReceiveAddress::getCity,
                    dto.getCity().trim()
            );
            hasUpdate = true;
        }

        if (dto.getRegion() != null) {
            wrapper.set(
                    UmsMemberReceiveAddress::getRegion,
                    dto.getRegion().trim()
            );
            hasUpdate = true;
        }

        if (dto.getDetailAddress() != null) {
            wrapper.set(
                    UmsMemberReceiveAddress::getDetailAddress,
                    dto.getDetailAddress().trim()
            );
            hasUpdate = true;
        }

        // 空 JSON 对象不执行数据库更新
        if (!hasUpdate) {
            return MemberAddressVO.from(current);
        }

        int updated = addressMapper.update(null, wrapper);

        if (updated == 0) {
            throw new BusinessException(
                    ErrorCode.ADDRESS_NOT_FOUND
            );
        }

        UmsMemberReceiveAddress updatedAddress =
                findOwnedAddress(memberId, addressId);

        return MemberAddressVO.from(updatedAddress);
    }

    @Override
    public void delete(
            Long memberId,
            Long addressId) {

        int deleted = addressMapper.delete(
                new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                        .eq(
                                UmsMemberReceiveAddress::getId,
                                addressId
                        )
                        .eq(
                                UmsMemberReceiveAddress::getMemberId,
                                memberId
                        )
        );

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.ADDRESS_NOT_FOUND
            );
        }
    }

    @Override
    @Transactional
    public void setDefault(
            Long memberId,
            Long addressId) {

        UmsMemberReceiveAddress address =
                findOwnedAddress(memberId, addressId);

        if (Integer.valueOf(1)
                .equals(address.getDefaultStatus())) {
            return;
        }

        clearDefault(memberId);

        int updated = addressMapper.update(
                null,
                new LambdaUpdateWrapper<UmsMemberReceiveAddress>()
                        .eq(
                                UmsMemberReceiveAddress::getId,
                                addressId
                        )
                        .eq(
                                UmsMemberReceiveAddress::getMemberId,
                                memberId
                        )
                        .set(
                                UmsMemberReceiveAddress::getDefaultStatus,
                                1
                        )
        );

        if (updated == 0) {
            throw new BusinessException(
                    ErrorCode.ADDRESS_NOT_FOUND
            );
        }
    }

    private UmsMemberReceiveAddress findOwnedAddress(
            Long memberId,
            Long addressId) {

        UmsMemberReceiveAddress address =
                addressMapper.selectOne(
                        new LambdaQueryWrapper<UmsMemberReceiveAddress>()
                                .eq(
                                        UmsMemberReceiveAddress::getId,
                                        addressId
                                )
                                .eq(
                                        UmsMemberReceiveAddress::getMemberId,
                                        memberId
                                )
                );

        if (address == null) {
            throw new BusinessException(
                    ErrorCode.ADDRESS_NOT_FOUND
            );
        }

        return address;
    }

    private void clearDefault(Long memberId) {

        addressMapper.update(
                null,
                new LambdaUpdateWrapper<UmsMemberReceiveAddress>()
                        .eq(
                                UmsMemberReceiveAddress::getMemberId,
                                memberId
                        )
                        .eq(
                                UmsMemberReceiveAddress::getDefaultStatus,
                                1
                        )
                        .set(
                                UmsMemberReceiveAddress::getDefaultStatus,
                                0
                        )
        );
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
}