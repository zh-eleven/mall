package com.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_product_attribute_value")
public class PmsProductAttributeValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Long productAttributeId;

    private String value;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}