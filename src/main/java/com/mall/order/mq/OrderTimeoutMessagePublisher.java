package com.mall.order.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(
            Long orderId,
            LocalDateTime expireTime) {

        OrderTimeoutMessage payload =
                new OrderTimeoutMessage(
                        orderId,
                        expireTime
                );

        CorrelationData correlationData =
                new CorrelationData(
                        UUID.randomUUID().toString()
                );

        rabbitTemplate.convertAndSend(
                RabbitOrderConfig.TIMEOUT_EXCHANGE,
                RabbitOrderConfig.TIMEOUT_ROUTING_KEY,
                payload,
                message -> {
                    message.getMessageProperties()
                            .setMessageId(
                                    correlationData.getId()
                            );

                    message.getMessageProperties()
                            .setDeliveryMode(
                                    MessageDeliveryMode.PERSISTENT
                            );

                    return message;
                },
                correlationData
        );

        correlationData.getFuture()
                .whenComplete((confirm, exception) -> {
                    if (exception != null) {
                        log.error(
                                "订单超时消息发送异常: orderId={}",
                                orderId,
                                exception
                        );
                        return;
                    }

                    var returned = correlationData.getReturned();

                    if (returned != null) {
                        log.error(
                                "订单超时消息无法路由: orderId={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                                orderId,
                                returned.getReplyCode(),
                                returned.getReplyText(),
                                returned.getExchange(),
                                returned.getRoutingKey()
                        );
                        return;
                    }

                    if (confirm.isAck()) {
                        log.info(
                                "订单超时消息发送成功: orderId={}",
                                orderId
                        );
                    } else {
                        log.error(
                                "订单超时消息发送失败: orderId={}, reason={}",
                                orderId,
                                confirm.getReason()
                        );
                    }
                });
    }
}