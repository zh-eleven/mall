package com.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.order.entity.OmsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OmsOrderMapper
        extends BaseMapper<OmsOrder> {

    /**
     * 仅允许会员取消自己的待支付订单。
     */
    @Update("""
            UPDATE oms_order
            SET status = #{canceledStatus},
                cancel_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND member_id = #{memberId}
              AND status = #{pendingStatus}
            """)
    int cancelPendingOrder(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId,
            @Param("pendingStatus") Integer pendingStatus,
            @Param("canceledStatus") Integer canceledStatus
    );

    /**
     * 仅允许会员支付自己的待支付订单。
     *
     * 状态条件用于避免支付与取消、重复支付之间的并发覆盖。
     */
    @Update("""
            UPDATE oms_order
            SET status = #{paidStatus},
                payment_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND member_id = #{memberId}
              AND status = #{pendingStatus}
            """)
    int payPendingOrder(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId,
            @Param("pendingStatus") Integer pendingStatus,
            @Param("paidStatus") Integer paidStatus
    );
}
