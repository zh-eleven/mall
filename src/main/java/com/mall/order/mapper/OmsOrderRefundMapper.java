package com.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.order.entity.OmsOrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OmsOrderRefundMapper
        extends BaseMapper<OmsOrderRefund> {

    @Update("""
            UPDATE oms_order_refund
            SET status = #{completedStatus},
                admin_note = #{adminNote},
                handle_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{refundId}
              AND status = #{applyingStatus}
            """)
    int completeApplyingRefund(
            @Param("refundId") Long refundId,
            @Param("applyingStatus") Integer applyingStatus,
            @Param("completedStatus") Integer completedStatus,
            @Param("adminNote") String adminNote
    );

    @Update("""
            UPDATE oms_order_refund
            SET status = #{rejectedStatus},
                admin_note = #{adminNote},
                handle_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{refundId}
              AND status = #{applyingStatus}
            """)
    int rejectApplyingRefund(
            @Param("refundId") Long refundId,
            @Param("applyingStatus") Integer applyingStatus,
            @Param("rejectedStatus") Integer rejectedStatus,
            @Param("adminNote") String adminNote
    );
}
