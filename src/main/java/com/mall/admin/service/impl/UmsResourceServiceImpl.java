package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.admin.dto.ResourceCreateDTO;
import com.mall.admin.dto.ResourceUpdateDTO;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.vo.ResourceVO;
import com.mall.admin.mapper.UmsResourceMapper;
import com.mall.admin.service.UmsResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UmsResourceServiceImpl
        implements UmsResourceService {

    private final UmsResourceMapper resourceMapper;

    @Override
    public ResourceVO create(ResourceCreateDTO dto) {

        String code = dto.getCode().trim();

        ensureCodeAvailable(code, null);

        UmsResource resource = new UmsResource();

        resource.setName(dto.getName().trim());
        resource.setCode(code);
        resource.setUrlPattern(
                normalizeText(dto.getUrlPattern())
        );
        resource.setHttpMethod(dto.getHttpMethod());
        resource.setDescription(
                normalizeText(dto.getDescription())
        );
        resource.setStatus(
                dto.getStatus() == null
                        ? 1
                        : dto.getStatus()
        );

        resourceMapper.insert(resource);

        return ResourceVO.from(
                findById(resource.getId())
        );
    }

    @Override
    public List<ResourceVO> list() {

        return resourceMapper.selectList(
                        new LambdaQueryWrapper<UmsResource>()
                                .orderByAsc(UmsResource::getId)
                )
                .stream()
                .map(ResourceVO::from)
                .toList();
    }

    @Override
    public ResourceVO update(
            Long resourceId,
            ResourceUpdateDTO dto) {

        findById(resourceId);

        LambdaUpdateWrapper<UmsResource> wrapper =
                new LambdaUpdateWrapper<UmsResource>()
                        .eq(
                                UmsResource::getId,
                                resourceId
                        );

        boolean hasUpdate = false;

        if (dto.getName() != null) {
            wrapper.set(
                    UmsResource::getName,
                    dto.getName().trim()
            );
            hasUpdate = true;
        }

        if (dto.getCode() != null) {
            String code = dto.getCode().trim();

            ensureCodeAvailable(code, resourceId);

            wrapper.set(
                    UmsResource::getCode,
                    code
            );
            hasUpdate = true;
        }

        if (dto.getUrlPattern() != null) {
            wrapper.set(
                    UmsResource::getUrlPattern,
                    normalizeText(dto.getUrlPattern())
            );
            hasUpdate = true;
        }

        if (dto.getHttpMethod() != null) {
            wrapper.set(
                    UmsResource::getHttpMethod,
                    dto.getHttpMethod()
            );
            hasUpdate = true;
        }

        if (dto.getDescription() != null) {
            wrapper.set(
                    UmsResource::getDescription,
                    normalizeText(dto.getDescription())
            );
            hasUpdate = true;
        }

        if (dto.getStatus() != null) {
            wrapper.set(
                    UmsResource::getStatus,
                    dto.getStatus()
            );
            hasUpdate = true;
        }

        if (hasUpdate) {
            int updated =
                    resourceMapper.update(null, wrapper);

            if (updated == 0) {
                throw new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND
                );
            }
        }

        return ResourceVO.from(
                findById(resourceId)
        );
    }

    @Override
    public void delete(Long resourceId) {

        int deleted =
                resourceMapper.deleteById(resourceId);

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private UmsResource findById(Long resourceId) {

        UmsResource resource =
                resourceMapper.selectById(resourceId);

        if (resource == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return resource;
    }

    private void ensureCodeAvailable(
            String code,
            Long excludedId) {

        LambdaQueryWrapper<UmsResource> query =
                new LambdaQueryWrapper<UmsResource>()
                        .eq(UmsResource::getCode, code);

        if (excludedId != null) {
            query.ne(
                    UmsResource::getId,
                    excludedId
            );
        }

        if (resourceMapper.selectCount(query) > 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_CODE_ALREADY_EXISTS
            );
        }
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
}