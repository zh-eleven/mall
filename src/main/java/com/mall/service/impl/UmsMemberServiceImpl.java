package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.domain.dto.MemberLoginDTO;
import com.mall.domain.dto.MemberRegisterDTO;
import com.mall.domain.entity.UmsMember;
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
}