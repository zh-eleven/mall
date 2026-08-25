package com.mall.order.mq;

import com.mall.order.config.OrderProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


@Configuration
public class RabbitOrderConfig {

    public static final String TIMEOUT_EXCHANGE =
            "mall.order.timeout.exchange";

    public static final String TIMEOUT_QUEUE =
            "mall.order.timeout.delay.queue";

    public static final String TIMEOUT_ROUTING_KEY =
            "mall.order.timeout";

    public static final String CANCEL_EXCHANGE =
            "mall.order.cancel.exchange";

    public static final String CANCEL_QUEUE =
            "mall.order.cancel.queue";

    public static final String CANCEL_ROUTING_KEY =
            "mall.order.cancel";

    public static final String DEAD_EXCHANGE =
            "mall.order.cancel.dead.exchange";

    public static final String DEAD_QUEUE =
            "mall.order.cancel.dead.queue";

    public static final String DEAD_ROUTING_KEY =
            "mall.order.cancel.dead";

    /*
     * 接收延迟消息的交换机。
     */
    @Bean
    public DirectExchange orderTimeoutExchange() {
        return ExchangeBuilder
                .directExchange(TIMEOUT_EXCHANGE)
                .durable(true)
                .build();
    }

    /*
     * 消息在该队列停留到订单超时时间后，
     * 通过死信交换机进入取消队列。
     */
    @Bean
    public Queue orderTimeoutQueue(
            OrderProperties orderProperties) {

        long timeoutMillis = Math.multiplyExact(
                orderProperties.getTimeoutMinutes(),
                60_000L
        );

        return QueueBuilder
                .durable(TIMEOUT_QUEUE)
                .withArgument(
                        "x-message-ttl",
                        timeoutMillis
                )
                .deadLetterExchange(CANCEL_EXCHANGE)
                .deadLetterRoutingKey(CANCEL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderTimeoutBinding(
            Queue orderTimeoutQueue,
            DirectExchange orderTimeoutExchange) {

        return BindingBuilder
                .bind(orderTimeoutQueue)
                .to(orderTimeoutExchange)
                .with(TIMEOUT_ROUTING_KEY);
    }

    /*
     * 超时消息实际消费使用的交换机。
     */
    @Bean
    public DirectExchange orderCancelExchange() {
        return ExchangeBuilder
                .directExchange(CANCEL_EXCHANGE)
                .durable(true)
                .build();
    }

    /*
     * 消费失败且不重新入队时，
     * 消息会进入失败队列。
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder
                .durable(CANCEL_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderCancelBinding(
            Queue orderCancelQueue,
            DirectExchange orderCancelExchange) {

        return BindingBuilder
                .bind(orderCancelQueue)
                .to(orderCancelExchange)
                .with(CANCEL_ROUTING_KEY);
    }

    /*
     * 最终消费失败消息的死信交换机与队列。
     */
    @Bean
    public DirectExchange orderCancelDeadExchange() {
        return ExchangeBuilder
                .directExchange(DEAD_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue orderCancelDeadQueue() {
        return QueueBuilder
                .durable(DEAD_QUEUE)
                .build();
    }

    @Bean
    public Binding orderCancelDeadBinding(
            Queue orderCancelDeadQueue,
            DirectExchange orderCancelDeadExchange) {

        return BindingBuilder
                .bind(orderCancelDeadQueue)
                .to(orderCancelDeadExchange)
                .with(DEAD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(
            ObjectMapper objectMapper) {

        return new Jackson2JsonMessageConverter(
                objectMapper
        );
    }
}