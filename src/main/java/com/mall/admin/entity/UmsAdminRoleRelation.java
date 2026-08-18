package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ums_admin_role_relation")
public class UmsAdminRoleRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;

    private Long roleId;

    private LocalDateTime createTime;
}