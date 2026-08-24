package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.entity.PmsSkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PmsSkuStockMapper
        extends BaseMapper<PmsSkuStock> {
    /**
     * 原子锁定库存。
     *
     * 返回1：锁定成功。
     * 返回0：SKU不存在、数量无效或可用库存不足。
     */
    @Update("""
            UPDATE pms_sku_stock
            SET locked_stock = locked_stock + #{quantity},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{skuId}
              AND #{quantity} > 0
              AND stock - locked_stock >= #{quantity}
            """)
    int lockStock(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );

    /**
     * 原子释放锁定库存。
     *
     * 返回1：释放成功。
     * 返回0：SKU不存在、数量无效或锁定库存不足。
     */
    @Update("""
        UPDATE pms_sku_stock
        SET locked_stock = locked_stock - #{quantity},
            update_time = CURRENT_TIMESTAMP
        WHERE id = #{skuId}
          AND #{quantity} > 0
          AND locked_stock >= #{quantity}
        """)
    int releaseLockedStock(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );

    /**
     * 支付成功后将锁定库存转为实际销量。
     *
     * 返回1：扣减成功。
     * 返回0：SKU不存在、数量无效或库存数据不一致。
     */
    @Update("""
            UPDATE pms_sku_stock
            SET stock = stock - #{quantity},
                locked_stock = locked_stock - #{quantity},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{skuId}
              AND #{quantity} > 0
              AND stock >= #{quantity}
              AND locked_stock >= #{quantity}
            """)
    int deductLockedStock(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );

    /**
     * 已支付订单退款通过后恢复实际库存。
     */
    @Update("""
            UPDATE pms_sku_stock
            SET stock = stock + #{quantity},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{skuId}
              AND #{quantity} > 0
            """)
    int restoreStock(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );
}
