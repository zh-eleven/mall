package com.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pms_sku_stock")
public class PmsSkuStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String skuCode;

    private BigDecimal price;

    /**
     * 总库存。
     */
    private Integer stock;

    /**
     * 下单后暂时锁定、尚未正式扣减的库存。
     */
    private Integer lockedStock;

    private Integer lowStock;

    private String pic;

    /**
     * 标准化规格组合，例如：
     * 1=黑色|2=16GB
     */
    private String specKey;

    /**
     * 规格组合的 JSON 内容。
     */
    private String specData;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}