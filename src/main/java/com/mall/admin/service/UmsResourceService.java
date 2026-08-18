package com.mall.admin.service;

import com.mall.admin.dto.ResourceCreateDTO;
import com.mall.admin.dto.ResourceUpdateDTO;
import com.mall.admin.vo.ResourceVO;

import java.util.List;

public interface UmsResourceService {

    ResourceVO create(ResourceCreateDTO dto);

    List<ResourceVO> list();

    ResourceVO update(
            Long resourceId,
            ResourceUpdateDTO dto
    );

    void delete(Long resourceId);
}