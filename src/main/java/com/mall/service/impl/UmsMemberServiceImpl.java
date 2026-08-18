package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.domain.dto.MemberLoginDTO;
import com.mall.domain.dto.MemberRegisterDTO;
import com.mall.domain.dto.MemberUpdateDTO;
import com.mall.domain.dto.MemberUpdatePasswordDTO;
import com.mall.domain.entity.UmsMember;
import com.mall.domain.vo.MemberInfoVO;
import com.mall.domain.vo.MemberLoginVO;
import com.mall.mapper.UmsMemberMapper;
import com.mall.security.JwtTokenService;
import com.mall.service.UmsMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UmsMemberServiceImpl implements UmsMemberService {

    private final UmsMemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    public void register(MemberRegisterDTO dto) {

        // 1. 检查用户名
        Long usernameCount = memberMapper.selectCount(
                new LambdaQueryWrapper<UmsMember>()
                        .eq(UmsMember::getUsername, dto.getUsername())
        );

        if (usernameCount > 0) {
            throw new BusinessException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }

        // 2. 检查手机号
        if (StringUtils.hasText(dto.getPhone())) {
            Long phoneCount = memberMapper.selectCount(
                    new LambdaQueryWrapper<UmsMember>()
                            .eq(UmsMember::getPhone, dto.getPhone())
            );

            if (phoneCount > 0) {
                throw new BusinessException(
                        ErrorCode.PHONE_ALREADY_EXISTS
                );
            }
        }

        // 3. 检查邮箱
        if (StringUtils.hasText(dto.getEmail())) {
            Long emailCount = memberMapper.selectCount(
                    new LambdaQueryWrapper<UmsMember>()
                            .eq(UmsMember::getEmail, dto.getEmail())
            );

            if (emailCount > 0) {
                throw new BusinessException(
                        ErrorCode.EMAIL_ALREADY_EXISTS
                );
            }
        }

        // 4. 创建用户
        UmsMember member = new UmsMember();

        member.setUsername(dto.getUsername());
        member.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        // 可选字段为空时存 null，避免唯一索引中的空字符串冲突
        member.setPhone(
                StringUtils.hasText(dto.getPhone())
                        ? dto.getPhone().trim()
                        : null
        );

        member.setEmail(
                StringUtils.hasText(dto.getEmail())
                        ? dto.getEmail().trim()
                        : null
        );

        member.setStatus(1);

        // 并发注册导致的唯一索引异常，
        // 由 GlobalExceptionHandler 统一转换为 409
        memberMapper.insert(member);
    }

    @Override
    public MemberLoginVO login(MemberLoginDTO dto) {

        // 1. 查询用户
        UmsMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<UmsMember>()
                        .eq(UmsMember::getUsername, dto.getUsername())
        );

        // 2. 用户不存在或密码错误，统一返回相同提示
        if (member == null
                || !passwordEncoder.matches(
                dto.getPassword(),
                member.getPassword()
        )) {

            throw new BusinessException(
                    ErrorCode.USERNAME_OR_PASSWORD_ERROR
            );
        }

        // 3. 检查账户状态，避免 Integer 拆箱产生空指针
        if (!Integer.valueOf(1).equals(member.getStatus())) {
            throw new BusinessException(
                    ErrorCode.MEMBER_DISABLED
            );
        }

        // 4. 生成 JWT
        String token = jwtTokenService.generateToken(member);

        // 5. 返回登录结果
        return new MemberLoginVO(
                member.getId(),
                member.getUsername(),
                token
        );
    }

    @Override
    public MemberInfoVO updateProfile(Long memberId, MemberUpdateDTO dto) {

        // 1. 确认用户存在
        UmsMember member = memberMapper.selectById(memberId);

        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 2. 手机号不为 null 才表示需要修改
        if (dto.getPhone() != null) {
            String phone = StringUtils.hasText(dto.getPhone())
                    ? dto.getPhone().trim()
                    : null;

            if (phone != null) {
                Long count = memberMapper.selectCount(
                        new LambdaQueryWrapper<UmsMember>()
                                .eq(UmsMember::getPhone, phone)
                                // 必须排除当前用户
                                .ne(UmsMember::getId, memberId)
                );

                if (count > 0) {
                    throw new BusinessException(
                            ErrorCode.PHONE_ALREADY_EXISTS
                    );
                }
            }
        }
        // 3. 邮箱不为 null 才表示需要修改
        if (dto.getEmail() != null) {
            String email = StringUtils.hasText(dto.getEmail())
                    ? dto.getEmail().trim()
                    : null;

            if (email != null) {
                Long count = memberMapper.selectCount(
                        new LambdaQueryWrapper<UmsMember>()
                                .eq(UmsMember::getEmail, email)
                                // 排除当前用户
                                .ne(UmsMember::getId, memberId)
                );

                if (count > 0) {
                    throw new BusinessException(
                            ErrorCode.EMAIL_ALREADY_EXISTS
                    );
                }
            }
        }
        // 4. 只更新请求中出现的字段
        LambdaUpdateWrapper<UmsMember> updateWrapper =
                new LambdaUpdateWrapper<UmsMember>()
                        .eq(UmsMember::getId, memberId);

        boolean hasUpdate = false;

        if (dto.getNickname() != null) {
            updateWrapper.set(
                    UmsMember::getNickname,
                    normalizeText(dto.getNickname())
            );
            hasUpdate = true;
        }

        if (dto.getPhone() != null) {
            updateWrapper.set(
                    UmsMember::getPhone,
                    normalizeText(dto.getPhone())
            );
            hasUpdate = true;
        }

        if (dto.getEmail() != null) {
            updateWrapper.set(
                    UmsMember::getEmail,
                    normalizeText(dto.getEmail())
            );
            hasUpdate = true;
        }

        if (dto.getAvatar() != null) {
            updateWrapper.set(
                    UmsMember::getAvatar,
                    normalizeText(dto.getAvatar())
            );
            hasUpdate = true;
        }

        if (dto.getGender() != null) {
            updateWrapper.set(
                    UmsMember::getGender,
                    dto.getGender()
            );
            hasUpdate = true;
        }

        if (dto.getBirthday() != null) {
            updateWrapper.set(
                    UmsMember::getBirthday,
                    dto.getBirthday()
            );
            hasUpdate = true;
        }

        if (hasUpdate) {
            memberMapper.update(null, updateWrapper);
        }

// 5. 重新查询，返回数据库中的最新资料
        UmsMember updatedMember = memberMapper.selectById(memberId);

        return MemberInfoVO.from(updatedMember);
    }

    @Override
    public void updatePassword(
            Long memberId,
            MemberUpdatePasswordDTO dto) {

        // 1. 查询当前用户
        UmsMember member = memberMapper.selectById(memberId);

        if (member == null) {
            throw new BusinessException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        // 2. 检查两次新密码
        if (!dto.getNewPassword().equals(
                dto.getConfirmPassword())) {

            throw new BusinessException(
                    ErrorCode.PASSWORD_CONFIRM_NOT_MATCH
            );
        }

        // 3. 验证原密码
        if (!passwordEncoder.matches(
                dto.getOldPassword(),
                member.getPassword())) {

            throw new BusinessException(
                    ErrorCode.OLD_PASSWORD_ERROR
            );
        }

        // 4. 新密码不能与原密码相同
        if (passwordEncoder.matches(
                dto.getNewPassword(),
                member.getPassword())) {

            throw new BusinessException(
                    ErrorCode.NEW_PASSWORD_SAME_AS_OLD
            );
        }

        // 5. 加密并更新密码
        String encodedPassword =
                passwordEncoder.encode(dto.getNewPassword());

        memberMapper.update(
                null,
                new LambdaUpdateWrapper<UmsMember>()
                        .eq(UmsMember::getId, memberId)
                        .set(
                                UmsMember::getPassword,
                                encodedPassword
                        )
        );
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
}