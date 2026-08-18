package com.mall.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.admin.entity.UmsAdminRoleRelation;
import com.mall.admin.entity.UmsResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UmsAdminRoleRelationMapper
        extends BaseMapper<UmsAdminRoleRelation> {

    @Select("""
            SELECT DISTINCT res.*
            FROM ums_admin_role_relation ar
            JOIN ums_role r
              ON r.id = ar.role_id
             AND r.status = 1
            JOIN ums_role_resource_relation rr
              ON rr.role_id = r.id
            JOIN ums_resource res
              ON res.id = rr.resource_id
             AND res.status = 1
            WHERE ar.admin_id = #{adminId}
            ORDER BY res.id
            """)
    List<UmsResource> selectResourceListByAdminId(
            @Param("adminId") Long adminId
    );
}