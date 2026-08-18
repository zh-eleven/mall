package com.mall.admin.controller;

import com.mall.common.api.ApiResult;
import com.mall.admin.dto.ResourceCreateDTO;
import com.mall.admin.dto.ResourceUpdateDTO;
import com.mall.admin.vo.ResourceVO;
import com.mall.admin.service.UmsResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/resources")
@RequiredArgsConstructor
public class UmsResourceController {

    private final UmsResourceService resourceService;

    @GetMapping
    @PreAuthorize("hasAuthority('resource:read')")
    public ApiResult<List<ResourceVO>> list() {
        return ApiResult.success(
                resourceService.list()
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('resource:write')")
    public ApiResult<ResourceVO> create(
            @Valid @RequestBody ResourceCreateDTO dto) {

        return ApiResult.success(
                resourceService.create(dto),
                "权限资源创建成功"
        );
    }

    @PatchMapping("/{resourceId}")
    @PreAuthorize("hasAuthority('resource:write')")
    public ApiResult<ResourceVO> update(
            @PathVariable Long resourceId,
            @Valid @RequestBody ResourceUpdateDTO dto) {

        return ApiResult.success(
                resourceService.update(resourceId, dto),
                "权限资源修改成功"
        );
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasAuthority('resource:write')")
    public ApiResult<Void> delete(
            @PathVariable Long resourceId) {

        resourceService.delete(resourceId);

        return ApiResult.success(
                null,
                "权限资源删除成功"
        );
    }
}