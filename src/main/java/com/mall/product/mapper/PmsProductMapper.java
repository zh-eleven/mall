package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.entity.PmsProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PmsProductMapper
        extends BaseMapper<PmsProduct> {

    /**
     * 支付成功后原子扣减商品汇总库存。
     */
    @Update("""
            UPDATE pms_product
            SET stock = stock - #{quantity},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{productId}
              AND #{quantity} > 0
              AND stock >= #{quantity}
            """)
    int decreaseStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );

    /**
     * 退款通过并入库后原子恢复商品汇总库存。
     */
    @Update("""
            UPDATE pms_product
            SET stock = stock + #{quantity},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{productId}
              AND #{quantity} > 0
            """)
    int increaseStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );
}
