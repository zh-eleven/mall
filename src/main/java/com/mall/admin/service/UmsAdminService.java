package com.mall.admin.service;

import com.mall.admin.dto.AdminCreateDTO;
import com.mall.admin.dto.AdminLoginDTO;
import com.mall.admin.dto.AdminUpdateDTO;
import com.mall.admin.vo.*;

import java.util.List;

public interface UmsAdminService {

    AdminLoginVO login(AdminLoginDTO dto);

    AdminInfoVO getInfo(Long adminId);

    AdminVO create(AdminCreateDTO dto);

    List<AdminVO> list();

    AdminVO update(
            Long operatorAdminId,
            Long targetAdminId,
            AdminUpdateDTO dto
    );

    void delete(
            Long operatorAdminId,
            Long targetAdminId
    );

    List<RoleVO> getRoles(Long adminId);

    void updateRoles(
            Long operatorAdminId,
            Long targetAdminId,
            List<Long> roleIds
    );
}