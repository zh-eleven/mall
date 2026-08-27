package com.mall.seckill.service;

import com.mall.seckill.mq.SeckillOrderMessage;

public interface SeckillOrderCreationService {

    /**
     * 根据MQ消息创建正式订单。
     *
     * @return 创建成功或此前已经创建的订单ID
     */
    Long createOrder(SeckillOrderMessage message);
}