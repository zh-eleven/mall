package com.mall.portal.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order_item")
public class OmsOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderSn;

    private Long productId;

    private Long skuId;

    /*
     * 以下是商品快照。
     * 商品以后改名、改图、改价格，不影响历史订单。
     */
    private String skuCode;

    private String productName;

    private String productPic;

    private String specData;

    /**
     * 下单时单价。
     */
    private BigDecimal productPrice;

    private Integer quantity;

    /**
     * productPrice × quantity。
     */
    private BigDecimal subtotal;

    private LocalDateTime createTime;
}