package com.mall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order")
public class OmsOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对外展示的订单编号，不使用数据库主键。
     */
    private String orderSn;

    private Long memberId;

    /**
     * 同一次下单请求的幂等令牌。
     */
    private String submitToken;

    private OrderStatus status;

    /**
     * 商品总金额。
     */
    private BigDecimal totalAmount;

    /**
     * 实际支付金额。
     * 当前没有优惠功能，暂时等于 totalAmount。
     */
    private BigDecimal payAmount;

    /*
     * 以下字段为下单时的收货地址快照。
     * 用户以后修改地址，不会影响历史订单。
     */
    private String receiverName;

    private String receiverPhone;

    private String receiverPostCode;

    private String receiverProvince;

    private String receiverCity;

    private String receiverRegion;

    private String receiverDetailAddress;

    /**
     * 用户订单备注。
     */
    private String note;

    private LocalDateTime paymentTime;

    private String deliveryCompany;

    private String deliverySn;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
