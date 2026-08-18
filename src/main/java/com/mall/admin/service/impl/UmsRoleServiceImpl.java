package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.admin.dto.RoleCreateDTO;
import com.mall.admin.dto.RoleUpdateDTO;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.entity.UmsRole;
import com.mall.admin.entity.UmsRoleResourceRelation;
import com.mall.admin.vo.ResourceVO;
import com.mall.admin.vo.RoleVO;
import com.mall.admin.mapper.UmsResourceMapper;
import com.mall.admin.mapper.UmsRoleMapper;
import com.mall.admin.mapper.UmsRoleResourceRelationMapper;
import com.mall.admin.service.UmsRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UmsRoleServiceImpl
        implements UmsRoleService {

    private static final String SUPER_ADMIN =
            "SUPER_ADMIN";

    private final UmsRoleMapper roleMapper;

    private final UmsResourceMapper resourceMapper;

    private final UmsRoleResourceRelationMapper
            roleResourceRelationMapper;

    @Override
    public RoleVO create(RoleCreateDTO dto) {

        String code = dto.getCode().trim();

        ensureCodeAvailable(code, null);

        UmsRole role = new UmsRole();

        role.setName(dto.getName().trim());
        role.setCode(code);
        role.setDescription(
                normalizeText(dto.getDescription())
        );
        role.setStatus(
                dto.getStatus() == null
                        ? 1
                        : dto.getStatus()
        );
        role.setSort(
                dto.getSort() == null
                        ? 0
                        : dto.getSort()
        );

        roleMapper.insert(role);

        return RoleVO.from(
                findById(role.getId())
        );
    }

    @Override
    public List<RoleVO> list() {

        return roleMapper.selectList(
                        new LambdaQueryWrapper<UmsRole>()
                                .orderByAsc(UmsRole::getSort)
                                .orderByAsc(UmsRole::getId)
                )
                .stream()
                .map(RoleVO::from)
                .toList();
    }

    @Override
    public RoleVO update(
            Long roleId,
            RoleUpdateDTO dto) {

        UmsRole current = findById(roleId);

        if (SUPER_ADMIN.equals(current.getCode())
                && (dto.getCode() != null
                || Integer.valueOf(0)
                .equals(dto.getStatus()))) {

            throw new BusinessException(
                    ErrorCode.ROLE_PROTECTED
            );
        }

        LambdaUpdateWrapper<UmsRole> wrapper =
                new LambdaUpdateWrapper<UmsRole>()
                        .eq(UmsRole::getId, roleId);

        boolean hasUpdate = false;

        if (dto.getName() != null) {
            wrapper.set(
                    UmsRole::getName,
                    dto.getName().trim()
            );
            hasUpdate = true;
        }

        if (dto.getCode() != null) {
            String code = dto.getCode().trim();

            ensureCodeAvailable(code, roleId);

            wrapper.set(UmsRole::getCode, code);
            hasUpdate = true;
        }

        if (dto.getDescription() != null) {
            wrapper.set(
                    UmsRole::getDescription,
                    normalizeText(dto.getDescription())
            );
            hasUpdate = true;
        }

        if (dto.getStatus() != null) {
            wrapper.set(
                    UmsRole::getStatus,
                    dto.getStatus()
            );
            hasUpdate = true;
        }

        if (dto.getSort() != null) {
            wrapper.set(
                    UmsRole::getSort,
                    dto.getSort()
            );
            hasUpdate = true;
        }

        if (hasUpdate) {
            int updated =
                    roleMapper.update(null, wrapper);

            if (updated == 0) {
                throw new BusinessException(
                        ErrorCode.ROLE_NOT_FOUND
                );
            }
        }

        return RoleVO.from(findById(roleId));
    }

    @Override
    public void delete(Long roleId) {

        UmsRole role = findById(roleId);

        if (SUPER_ADMIN.equals(role.getCode())) {
            throw new BusinessException(
                    ErrorCode.ROLE_PROTECTED
            );
        }

        int deleted = roleMapper.deleteById(roleId);

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.ROLE_NOT_FOUND
            );
        }
    }

    @Override
    public List<ResourceVO> getResources(Long roleId) {

        findById(roleId);

        List<UmsRoleResourceRelation> relations =
                roleResourceRelationMapper.selectList(
                        new LambdaQueryWrapper
                                <UmsRoleResourceRelation>()
                                .eq(
                                        UmsRoleResourceRelation::getRoleId,
                                        roleId
                                )
                );

        if (relations.isEmpty()) {
            return List.of();
        }

        List<Long> resourceIds = relations.stream()
                .map(
                        UmsRoleResourceRelation::getResourceId
                )
                .distinct()
                .toList();

        return resourceMapper
                .selectBatchIds(resourceIds)
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                UmsResource::getId
                        )
                )
                .map(ResourceVO::from)
                .toList();
    }

    @Override
    @Transactional
    public void updateResources(
            Long roleId,
            List<Long> resourceIds) {

        UmsRole role = findById(roleId);

        if (SUPER_ADMIN.equals(role.getCode())) {
            throw new BusinessException(
                    ErrorCode.ROLE_PROTECTED
            );
        }

        Set<Long> uniqueIds =
                new LinkedHashSet<>(resourceIds);

        if (!uniqueIds.isEmpty()) {
            Long validCount =
                    resourceMapper.selectCount(
                            new LambdaQueryWrapper<UmsResource>()
                                    .in(
                                            UmsResource::getId,
                                            uniqueIds
                                    )
                                    .eq(
                                            UmsResource::getStatus,
                                            1
                                    )
                    );

            if (validCount != uniqueIds.size()) {
                throw new BusinessException(
                        ErrorCode.RESOURCE_SELECTION_INVALID
                );
            }
        }

        roleResourceRelationMapper.delete(
                new LambdaQueryWrapper
                        <UmsRoleResourceRelation>()
                        .eq(
                                UmsRoleResourceRelation::getRoleId,
                                roleId
                        )
        );

        for (Long resourceId : uniqueIds) {
            UmsRoleResourceRelation relation =
                    new UmsRoleResourceRelation();

            relation.setRoleId(roleId);
            relation.setResourceId(resourceId);

            roleResourceRelationMapper.insert(relation);
        }
    }

    private UmsRole findById(Long roleId) {

        UmsRole role = roleMapper.selectById(roleId);

        if (role == null) {
            throw new BusinessException(
                    ErrorCode.ROLE_NOT_FOUND
            );
        }

        return role;
    }

    private void ensureCodeAvailable(
            String code,
            Long excludedId) {

        LambdaQueryWrapper<UmsRole> query =
                new LambdaQueryWrapper<UmsRole>()
                        .eq(UmsRole::getCode, code);

        if (excludedId != null) {
            query.ne(UmsRole::getId, excludedId);
        }

        if (roleMapper.selectCount(query) > 0) {
            throw new BusinessException(
                    ErrorCode.ROLE_CODE_ALREADY_EXISTS
            );
        }
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
}