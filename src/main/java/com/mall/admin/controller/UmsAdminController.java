package com.mall.admin.controller;

import com.mall.common.api.ApiResult;
import com.mall.admin.dto.AdminLoginDTO;
import com.mall.admin.vo.AdminInfoVO;
import com.mall.admin.vo.AdminLoginVO;
import com.mall.security.AdminDetails;
import com.mall.admin.service.UmsAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.mall.admin.dto.AdminCreateDTO;
import com.mall.admin.dto.AdminRoleUpdateDTO;
import com.mall.admin.dto.AdminUpdateDTO;
import com.mall.admin.vo.AdminVO;
import com.mall.admin.vo.RoleVO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UmsAdminController {

    private final UmsAdminService adminService;

    @PostMapping("/auth/login")
    public ApiResult<AdminLoginVO> login(
            @Valid @RequestBody AdminLoginDTO dto) {

        AdminLoginVO result =
                adminService.login(dto);

        return ApiResult.success(
                result,
                "管理员登录成功"
        );
    }

    @GetMapping("/me")
    public ApiResult<AdminInfoVO> me(
            @AuthenticationPrincipal
            AdminDetails adminDetails) {

        Long adminId =
                adminDetails.getAdmin().getId();

        return ApiResult.success(
                adminService.getInfo(adminId)
        );
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('admin:read')")
    public ApiResult<List<AdminVO>> list() {
        return ApiResult.success(
                adminService.list()
        );
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('admin:write')")
    public ApiResult<AdminVO> create(
            @Valid @RequestBody AdminCreateDTO dto) {

        return ApiResult.success(
                adminService.create(dto),
                "管理员创建成功"
        );
    }

    @PatchMapping("/users/{adminId}")
    @PreAuthorize("hasAuthority('admin:write')")
    public ApiResult<AdminVO> update(
            @AuthenticationPrincipal
            AdminDetails adminDetails,
            @PathVariable Long adminId,
            @Valid @RequestBody AdminUpdateDTO dto) {

        Long operatorId =
                adminDetails.getAdmin().getId();

        return ApiResult.success(
                adminService.update(
                        operatorId,
                        adminId,
                        dto
                ),
                "管理员修改成功"
        );
    }

    @DeleteMapping("/users/{adminId}")
    @PreAuthorize("hasAuthority('admin:write')")
    public ApiResult<Void> delete(
            @AuthenticationPrincipal
            AdminDetails adminDetails,
            @PathVariable Long adminId) {

        Long operatorId =
                adminDetails.getAdmin().getId();

        adminService.delete(operatorId, adminId);

        return ApiResult.success(
                null,
                "管理员删除成功"
        );
    }

    @GetMapping("/users/{adminId}/roles")
    @PreAuthorize("hasAuthority('admin:read')")
    public ApiResult<List<RoleVO>> getRoles(
            @PathVariable Long adminId) {

        return ApiResult.success(
                adminService.getRoles(adminId)
        );
    }

    @PutMapping("/users/{adminId}/roles")
    @PreAuthorize("hasAuthority('admin:write')")
    public ApiResult<Void> updateRoles(
            @AuthenticationPrincipal
            AdminDetails adminDetails,
            @PathVariable Long adminId,
            @Valid @RequestBody
            AdminRoleUpdateDTO dto) {

        Long operatorId =
                adminDetails.getAdmin().getId();

        adminService.updateRoles(
                operatorId,
                adminId,
                dto.getRoleIds()
        );

        return ApiResult.success(
                null,
                "管理员角色分配成功"
        );
    }
}