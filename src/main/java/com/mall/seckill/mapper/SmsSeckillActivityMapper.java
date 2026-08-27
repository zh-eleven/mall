package com.mall.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.seckill.entity.SmsSeckillActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SmsSeckillActivityMapper
        extends BaseMapper<SmsSeckillActivity> {

    @Select("""
            SELECT *
            FROM sms_seckill_activity
            WHERE id = #{activityId}
            FOR UPDATE
            """)
    SmsSeckillActivity selectByIdForUpdate(
            @Param("activityId") Long activityId
    );
}
