package com.mall.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.seckill.entity.SmsSeckillSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SmsSeckillSkuMapper
        extends BaseMapper<SmsSeckillSku> {

    /**
     * MQ消费者创建订单时扣减数据库秒杀库存。
     *
     * 返回1：扣减成功。
     * 返回0：记录不存在、数量无效或库存不足。
     */
    @Update("""
            UPDATE sms_seckill_sku
            SET available_stock =
                    available_stock - #{quantity},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{seckillSkuId}
              AND #{quantity} > 0
              AND available_stock >= #{quantity}
            """)
    int decreaseAvailableStock(
            @Param("seckillSkuId")
            Long seckillSkuId,

            @Param("quantity")
            Integer quantity
    );
}