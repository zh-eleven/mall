package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.admin.dto.AdminCreateDTO;
import com.mall.admin.dto.AdminLoginDTO;
import com.mall.admin.dto.AdminUpdateDTO;
import com.mall.admin.entity.UmsAdmin;
import com.mall.admin.entity.UmsAdminRoleRelation;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.entity.UmsRole;
import com.mall.admin.vo.AdminInfoVO;
import com.mall.admin.vo.AdminLoginVO;
import com.mall.admin.vo.AdminVO;
import com.mall.admin.vo.RoleVO;
import com.mall.admin.mapper.UmsAdminMapper;
import com.mall.admin.mapper.UmsAdminRoleRelationMapper;
import com.mall.admin.mapper.UmsRoleMapper;
import com.mall.security.JwtTokenService;
import com.mall.admin.service.UmsAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UmsAdminServiceImpl
        implements UmsAdminService {

    private final UmsAdminMapper adminMapper;

    private final UmsAdminRoleRelationMapper
            adminRoleRelationMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final UmsRoleMapper roleMapper;


    @Override
    public AdminLoginVO login(AdminLoginDTO dto) {

        String username = dto.getUsername().trim();

        UmsAdmin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<UmsAdmin>()
                        .eq(
                                UmsAdmin::getUsername,
                                username
                        )
        );

        // 用户不存在与密码错误返回相同信息
        if (admin == null
                || !passwordEncoder.matches(
                dto.getPassword(),
                admin.getPassword()
        )) {

            throw new BusinessException(
                    ErrorCode.USERNAME_OR_PASSWORD_ERROR
            );
        }

        if (!Integer.valueOf(1)
                .equals(admin.getStatus())) {

            throw new BusinessException(
                    ErrorCode.ADMIN_DISABLED
            );
        }

        String token =
                jwtTokenService.generateToken(admin);

        adminMapper.update(
                null,
                new LambdaUpdateWrapper<UmsAdmin>()
                        .eq(
                                UmsAdmin::getId,
                                admin.getId()
                        )
                        .set(
                                UmsAdmin::getLoginTime,
                                LocalDateTime.now()
                        )
        );

        return new AdminLoginVO(
                admin.getId(),
                admin.getUsername(),
                token
        );
    }

    @Override
    public AdminInfoVO getInfo(Long adminId) {

        UmsAdmin admin =
                adminMapper.selectById(adminId);

        if (admin == null) {
            throw new BusinessException(
                    ErrorCode.ADMIN_NOT_FOUND
            );
        }

        List<UmsResource> resources =
                adminRoleRelationMapper
                        .selectResourceListByAdminId(
                                adminId
                        );

        return AdminInfoVO.from(
                admin,
                resources
        );
    }
    @Override
    public AdminVO create(AdminCreateDTO dto) {

        String username = dto.getUsername().trim();
        String email = normalizeEmail(dto.getEmail());

        ensureUsernameAvailable(username);

        if (email != null) {
            ensureEmailAvailable(email, null);
        }

        UmsAdmin admin = new UmsAdmin();

        admin.setUsername(username);
        admin.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );
        admin.setNickname(
                normalizeText(dto.getNickname())
        );
        admin.setEmail(email);
        admin.setAvatar(
                normalizeText(dto.getAvatar())
        );
        admin.setNote(
                normalizeText(dto.getNote())
        );
        admin.setStatus(
                dto.getStatus() == null
                        ? 1
                        : dto.getStatus()
        );

        adminMapper.insert(admin);

        return AdminVO.from(
                findAdminById(admin.getId())
        );
    }

    @Override
    public List<AdminVO> list() {

        return adminMapper.selectList(
                        new LambdaQueryWrapper<UmsAdmin>()
                                .orderByAsc(UmsAdmin::getId)
                )
                .stream()
                .map(AdminVO::from)
                .toList();
    }

    @Override
    public AdminVO update(
            Long operatorAdminId,
            Long targetAdminId,
            AdminUpdateDTO dto) {

        findAdminById(operatorAdminId);
        findAdminById(targetAdminId);

        if (operatorAdminId.equals(targetAdminId)
                && Integer.valueOf(0)
                .equals(dto.getStatus())) {

            throw new BusinessException(
                    ErrorCode.ADMIN_SELF_OPERATION_FORBIDDEN
            );
        }

        if (Integer.valueOf(0).equals(dto.getStatus())
                && isSuperAdmin(targetAdminId)) {

            throw new BusinessException(
                    ErrorCode.SUPER_ADMIN_PROTECTED
            );
        }

        LambdaUpdateWrapper<UmsAdmin> wrapper =
                new LambdaUpdateWrapper<UmsAdmin>()
                        .eq(UmsAdmin::getId, targetAdminId);

        boolean hasUpdate = false;

        if (dto.getNickname() != null) {
            wrapper.set(
                    UmsAdmin::getNickname,
                    normalizeText(dto.getNickname())
            );
            hasUpdate = true;
        }

        if (dto.getEmail() != null) {
            String email = normalizeEmail(dto.getEmail());

            if (email != null) {
                ensureEmailAvailable(
                        email,
                        targetAdminId
                );
            }

            wrapper.set(UmsAdmin::getEmail, email);
            hasUpdate = true;
        }

        if (dto.getAvatar() != null) {
            wrapper.set(
                    UmsAdmin::getAvatar,
                    normalizeText(dto.getAvatar())
            );
            hasUpdate = true;
        }

        if (dto.getNote() != null) {
            wrapper.set(
                    UmsAdmin::getNote,
                    normalizeText(dto.getNote())
            );
            hasUpdate = true;
        }

        if (dto.getStatus() != null) {
            wrapper.set(
                    UmsAdmin::getStatus,
                    dto.getStatus()
            );
            hasUpdate = true;
        }

        if (hasUpdate) {
            int updated = adminMapper.update(null, wrapper);

            if (updated == 0) {
                throw new BusinessException(
                        ErrorCode.ADMIN_NOT_FOUND
                );
            }
        }

        return AdminVO.from(
                findAdminById(targetAdminId)
        );
    }

    @Override
    public void delete(
            Long operatorAdminId,
            Long targetAdminId) {

        findAdminById(operatorAdminId);
        findAdminById(targetAdminId);

        if (operatorAdminId.equals(targetAdminId)) {
            throw new BusinessException(
                    ErrorCode.ADMIN_SELF_OPERATION_FORBIDDEN
            );
        }

        if (isSuperAdmin(targetAdminId)) {
            throw new BusinessException(
                    ErrorCode.SUPER_ADMIN_PROTECTED
            );
        }

        int deleted =
                adminMapper.deleteById(targetAdminId);

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.ADMIN_NOT_FOUND
            );
        }
    }

    @Override
    public List<RoleVO> getRoles(Long adminId) {

        findAdminById(adminId);

        List<UmsAdminRoleRelation> relations =
                adminRoleRelationMapper.selectList(
                        new LambdaQueryWrapper
                                <UmsAdminRoleRelation>()
                                .eq(
                                        UmsAdminRoleRelation::getAdminId,
                                        adminId
                                )
                );

        if (relations.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = relations.stream()
                .map(UmsAdminRoleRelation::getRoleId)
                .distinct()
                .toList();

        return roleMapper.selectBatchIds(roleIds)
                .stream()
                .sorted(
                        java.util.Comparator
                                .comparing(UmsRole::getSort)
                                .thenComparing(UmsRole::getId)
                )
                .map(RoleVO::from)
                .toList();
    }

    @Override
    @Transactional
    public void updateRoles(
            Long operatorAdminId,
            Long targetAdminId,
            List<Long> roleIds) {

        findAdminById(operatorAdminId);
        findAdminById(targetAdminId);

        if (operatorAdminId.equals(targetAdminId)) {
            throw new BusinessException(
                    ErrorCode.ADMIN_SELF_OPERATION_FORBIDDEN
            );
        }

        Set<Long> uniqueIds =
                new LinkedHashSet<>(roleIds);

        if (!uniqueIds.isEmpty()) {
            Long validCount = roleMapper.selectCount(
                    new LambdaQueryWrapper<UmsRole>()
                            .in(UmsRole::getId, uniqueIds)
                            .eq(UmsRole::getStatus, 1)
            );

            if (validCount != uniqueIds.size()) {
                throw new BusinessException(
                        ErrorCode.ROLE_SELECTION_INVALID
                );
            }
        }

        UmsRole superAdminRole = findSuperAdminRole();

        boolean operatorIsSuper =
                isSuperAdmin(operatorAdminId);

        boolean targetWasSuper =
                isSuperAdmin(targetAdminId);

        boolean targetWillBeSuper =
                superAdminRole != null
                        && uniqueIds.contains(
                        superAdminRole.getId()
                );

        if ((targetWasSuper || targetWillBeSuper)
                && !operatorIsSuper) {

            throw new BusinessException(
                    ErrorCode.ADMIN_ROLE_OPERATION_FORBIDDEN
            );
        }

        if (targetWasSuper
                && !targetWillBeSuper
                && countEnabledSuperAdmins() <= 1) {

            throw new BusinessException(
                    ErrorCode.LAST_SUPER_ADMIN_PROTECTED
            );
        }

        adminRoleRelationMapper.delete(
                new LambdaQueryWrapper
                        <UmsAdminRoleRelation>()
                        .eq(
                                UmsAdminRoleRelation::getAdminId,
                                targetAdminId
                        )
        );

        for (Long roleId : uniqueIds) {
            UmsAdminRoleRelation relation =
                    new UmsAdminRoleRelation();

            relation.setAdminId(targetAdminId);
            relation.setRoleId(roleId);

            adminRoleRelationMapper.insert(relation);
        }
    }

    private UmsAdmin findAdminById(Long adminId) {

        UmsAdmin admin =
                adminMapper.selectById(adminId);

        if (admin == null) {
            throw new BusinessException(
                    ErrorCode.ADMIN_NOT_FOUND
            );
        }

        return admin;
    }

    private void ensureUsernameAvailable(
            String username) {

        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<UmsAdmin>()
                        .eq(
                                UmsAdmin::getUsername,
                                username
                        )
        );

        if (count > 0) {
            throw new BusinessException(
                    ErrorCode.ADMIN_USERNAME_ALREADY_EXISTS
            );
        }
    }

    private void ensureEmailAvailable(
            String email,
            Long excludedId) {

        LambdaQueryWrapper<UmsAdmin> query =
                new LambdaQueryWrapper<UmsAdmin>()
                        .eq(UmsAdmin::getEmail, email);

        if (excludedId != null) {
            query.ne(UmsAdmin::getId, excludedId);
        }

        if (adminMapper.selectCount(query) > 0) {
            throw new BusinessException(
                    ErrorCode.ADMIN_EMAIL_ALREADY_EXISTS
            );
        }
    }

    private UmsRole findSuperAdminRole() {
        return roleMapper.selectOne(
                new LambdaQueryWrapper<UmsRole>()
                        .eq(
                                UmsRole::getCode,
                                SUPER_ADMIN
                        )
        );
    }

    private boolean isSuperAdmin(Long adminId) {

        UmsRole role = findSuperAdminRole();

        if (role == null) {
            return false;
        }

        return adminRoleRelationMapper.selectCount(
                new LambdaQueryWrapper
                        <UmsAdminRoleRelation>()
                        .eq(
                                UmsAdminRoleRelation::getAdminId,
                                adminId
                        )
                        .eq(
                                UmsAdminRoleRelation::getRoleId,
                                role.getId()
                        )
        ) > 0;
    }

    private long countEnabledSuperAdmins() {

        UmsRole role = findSuperAdminRole();

        if (role == null) {
            return 0;
        }

        List<Long> adminIds =
                adminRoleRelationMapper.selectList(
                                new LambdaQueryWrapper
                                        <UmsAdminRoleRelation>()
                                        .eq(
                                                UmsAdminRoleRelation::getRoleId,
                                                role.getId()
                                        )
                        )
                        .stream()
                        .map(
                                UmsAdminRoleRelation::getAdminId
                        )
                        .distinct()
                        .toList();

        if (adminIds.isEmpty()) {
            return 0;
        }

        return adminMapper.selectCount(
                new LambdaQueryWrapper<UmsAdmin>()
                        .in(UmsAdmin::getId, adminIds)
                        .eq(UmsAdmin::getStatus, 1)
        );
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private String normalizeEmail(String value) {
        return StringUtils.hasText(value)
                ? value.trim().toLowerCase(
                java.util.Locale.ROOT
        )
                : null;
    }
}