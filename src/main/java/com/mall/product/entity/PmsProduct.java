package com.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pms_product")
public class PmsProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long brandId;

    private Long productCategoryId;

    private String name;

    private String subTitle;

    private String productSn;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer lowStock;

    private String unit;

    private BigDecimal weight;

    private Integer publishStatus;

    private Integer newStatus;

    private Integer recommendStatus;

    private Integer verifyStatus;

    private Integer sort;

    private String pic;

    private String albumPics;

    private String description;

    private String detailTitle;

    private String detailDesc;

    private String detailHtml;

    @TableLogic(value = "0", delval = "1")
    private Integer deleteStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}