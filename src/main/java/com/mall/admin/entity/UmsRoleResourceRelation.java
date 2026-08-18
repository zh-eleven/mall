package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ums_role_resource_relation")
public class UmsRoleResourceRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;

    private Long resourceId;

    private LocalDateTime createTime;
}