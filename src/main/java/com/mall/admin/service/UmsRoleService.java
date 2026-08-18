package com.mall.admin.service;

import com.mall.admin.dto.RoleCreateDTO;
import com.mall.admin.dto.RoleUpdateDTO;
import com.mall.admin.vo.ResourceVO;
import com.mall.admin.vo.RoleVO;

import java.util.List;

public interface UmsRoleService {

    RoleVO create(RoleCreateDTO dto);

    List<RoleVO> list();

    RoleVO update(
            Long roleId,
            RoleUpdateDTO dto
    );

    void delete(Long roleId);

    List<ResourceVO> getResources(Long roleId);

    void updateResources(
            Long roleId,
            List<Long> resourceIds
    );
}