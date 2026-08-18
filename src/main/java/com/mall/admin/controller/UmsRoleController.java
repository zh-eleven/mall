package com.mall.admin.controller;

import com.mall.common.api.ApiResult;
import com.mall.admin.dto.RoleCreateDTO;
import com.mall.admin.dto.RoleResourceUpdateDTO;
import com.mall.admin.dto.RoleUpdateDTO;
import com.mall.admin.vo.ResourceVO;
import com.mall.admin.vo.RoleVO;
import com.mall.admin.service.UmsRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class UmsRoleController {

    private final UmsRoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public ApiResult<List<RoleVO>> list() {
        return ApiResult.success(roleService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:write')")
    public ApiResult<RoleVO> create(
            @Valid @RequestBody RoleCreateDTO dto) {

        return ApiResult.success(
                roleService.create(dto),
                "角色创建成功"
        );
    }

    @PatchMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:write')")
    public ApiResult<RoleVO> update(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateDTO dto) {

        return ApiResult.success(
                roleService.update(roleId, dto),
                "角色修改成功"
        );
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:write')")
    public ApiResult<Void> delete(
            @PathVariable Long roleId) {

        roleService.delete(roleId);

        return ApiResult.success(
                null,
                "角色删除成功"
        );
    }

    @GetMapping("/{roleId}/resources")
    @PreAuthorize("hasAuthority('role:read')")
    public ApiResult<List<ResourceVO>> getResources(
            @PathVariable Long roleId) {

        return ApiResult.success(
                roleService.getResources(roleId)
        );
    }

    @PutMapping("/{roleId}/resources")
    @PreAuthorize("hasAuthority('role:write')")
    public ApiResult<Void> updateResources(
            @PathVariable Long roleId,
            @Valid @RequestBody
            RoleResourceUpdateDTO dto) {

        roleService.updateResources(
                roleId,
                dto.getResourceIds()
        );

        return ApiResult.success(
                null,
                "角色资源分配成功"
        );
    }
}