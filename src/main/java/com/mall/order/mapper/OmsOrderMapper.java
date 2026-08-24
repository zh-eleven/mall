package com.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.order.entity.OmsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 管理员仅能将待发货订单原子更新为已发货。
     */
    @Update("""
            UPDATE oms_order
            SET status = #{shippedStatus},
                delivery_company = #{deliveryCompany},
                delivery_sn = #{deliverySn},
                delivery_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = #{pendingShipmentStatus}
            """)
    int shipPendingOrder(
            @Param("orderId") Long orderId,
            @Param("deliveryCompany") String deliveryCompany,
            @Param("deliverySn") String deliverySn,
            @Param("pendingShipmentStatus") Integer pendingShipmentStatus,
            @Param("shippedStatus") Integer shippedStatus
    );

    /**
     * 会员仅能确认自己已发货的订单。
     */
    @Update("""
            UPDATE oms_order
            SET status = #{completedStatus},
                receive_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND member_id = #{memberId}
              AND status = #{shippedStatus}
            """)
    int confirmShippedOrder(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId,
            @Param("shippedStatus") Integer shippedStatus,
            @Param("completedStatus") Integer completedStatus
    );

    @Select("""
            SELECT id
            FROM oms_order
            WHERE status = #{pendingStatus}
              AND create_time < #{cutoffTime}
            ORDER BY create_time, id
            LIMIT #{batchSize}
            """)
    List<Long> selectTimedOutPendingOrderIds(
            @Param("pendingStatus") Integer pendingStatus,
            @Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("batchSize") Integer batchSize
    );

    /**
     * 支付与超时取消竞争时，只有一个状态更新能够成功。
     */
    @Update("""
            UPDATE oms_order
            SET status = #{canceledStatus},
                cancel_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = #{pendingStatus}
              AND create_time < #{cutoffTime}
            """)
    int cancelTimedOutPendingOrder(
            @Param("orderId") Long orderId,
            @Param("pendingStatus") Integer pendingStatus,
            @Param("canceledStatus") Integer canceledStatus,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    /**
     * 会员申请退款时，与后台发货竞争同一个待发货状态。
     */
    @Update("""
            UPDATE oms_order
            SET status = #{refundingStatus},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND member_id = #{memberId}
              AND status = #{pendingShipmentStatus}
            """)
    int markRefunding(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId,
            @Param("pendingShipmentStatus") Integer pendingShipmentStatus,
            @Param("refundingStatus") Integer refundingStatus
    );

    @Update("""
            UPDATE oms_order
            SET status = #{refundedStatus},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = #{refundingStatus}
            """)
    int markRefunded(
            @Param("orderId") Long orderId,
            @Param("refundingStatus") Integer refundingStatus,
            @Param("refundedStatus") Integer refundedStatus
    );

    @Update("""
            UPDATE oms_order
            SET status = #{pendingShipmentStatus},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = #{refundingStatus}
            """)
    int restorePendingShipment(
            @Param("orderId") Long orderId,
            @Param("refundingStatus") Integer refundingStatus,
            @Param("pendingShipmentStatus") Integer pendingShipmentStatus
    );
}
