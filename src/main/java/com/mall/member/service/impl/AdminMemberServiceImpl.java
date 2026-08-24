package com.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.member.dto.AdminMemberQueryDTO;
import com.mall.member.dto.MemberStatusUpdateDTO;
import com.mall.member.entity.UmsMember;
import com.mall.member.mapper.UmsMemberMapper;
import com.mall.member.service.AdminMemberService;
import com.mall.member.vo.AdminMemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberServiceImpl
        implements AdminMemberService {

    private static final Set<Integer> VALID_STATUSES =
            Set.of(0, 1);

    private final UmsMemberMapper memberMapper;

    @Override
    public PageResult<AdminMemberVO> page(
            AdminMemberQueryDTO query) {

        validateStatus(query.getStatus());

        LambdaQueryWrapper<UmsMember> wrapper =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(
                    UmsMember::getUsername,
                    query.getUsername().trim()
            );
        }

        if (StringUtils.hasText(query.getPhone())) {
            wrapper.like(
                    UmsMember::getPhone,
                    query.getPhone().trim()
            );
        }

        if (query.getStatus() != null) {
            wrapper.eq(
                    UmsMember::getStatus,
                    query.getStatus()
            );
        }

        wrapper.orderByDesc(UmsMember::getCreateTime)
                .orderByDesc(UmsMember::getId);

        Page<UmsMember> page = memberMapper.selectPage(
                new Page<>(
                        query.getPageNum(),
                        query.getPageSize()
                ),
                wrapper
        );

        return PageResult.from(page, AdminMemberVO::from);
    }

    @Override
    public AdminMemberVO getDetail(Long memberId) {
        return AdminMemberVO.from(findMember(memberId));
    }

    @Override
    @Transactional
    public AdminMemberVO updateStatus(
            Long memberId,
            MemberStatusUpdateDTO dto) {

        Integer targetStatus = dto.getStatus();
        validateStatus(targetStatus);

        UmsMember member = findMember(memberId);

        if (targetStatus.equals(member.getStatus())) {
            return AdminMemberVO.from(member);
        }

        int updated = memberMapper.updateStatusIfCurrent(
                memberId,
                member.getStatus(),
                targetStatus
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.MEMBER_CONCURRENT_OPERATION
            );
        }

        return AdminMemberVO.from(findMember(memberId));
    }

    private UmsMember findMember(Long memberId) {
        UmsMember member = memberMapper.selectById(memberId);

        if (member == null) {
            throw new BusinessException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        return member;
    }

    private void validateStatus(Integer status) {
        if (status != null && !VALID_STATUSES.contains(status)) {
            throw new BusinessException(
                    ErrorCode.MEMBER_STATUS_INVALID
            );
        }
    }
}
