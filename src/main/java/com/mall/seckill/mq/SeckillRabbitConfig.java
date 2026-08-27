package com.mall.seckill.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeckillRabbitConfig {

    public static final String ORDER_EXCHANGE =
            "mall.seckill.order.exchange";

    public static final String ORDER_QUEUE =
            "mall.seckill.order.queue";

    public static final String ORDER_ROUTING_KEY =
            "mall.seckill.order.create";

    public static final String DEAD_EXCHANGE =
            "mall.seckill.order.dlx";

    public static final String DEAD_QUEUE =
            "mall.seckill.order.dead.queue";

    public static final String DEAD_ROUTING_KEY =
            "mall.seckill.order.dead";

    public static final String PARKING_EXCHANGE =
            "mall.seckill.order.parking.exchange";

    public static final String PARKING_QUEUE =
            "mall.seckill.order.parking.queue";

    public static final String PARKING_ROUTING_KEY =
            "mall.seckill.order.parking";

    @Bean
    public DirectExchange seckillOrderExchange() {
        return new DirectExchange(
                ORDER_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder
                .durable(ORDER_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding seckillOrderBinding(
            Queue seckillOrderQueue,
            DirectExchange seckillOrderExchange) {

        return BindingBuilder
                .bind(seckillOrderQueue)
                .to(seckillOrderExchange)
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public DirectExchange seckillDeadExchange() {
        return new DirectExchange(
                DEAD_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue seckillDeadQueue() {
        return QueueBuilder
                .durable(DEAD_QUEUE)
                .deadLetterExchange(
                        PARKING_EXCHANGE
                )
                .deadLetterRoutingKey(
                        PARKING_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding seckillDeadBinding(
            Queue seckillDeadQueue,
            DirectExchange seckillDeadExchange) {

        return BindingBuilder
                .bind(seckillDeadQueue)
                .to(seckillDeadExchange)
                .with(DEAD_ROUTING_KEY);
    }


    @Bean
    public DirectExchange seckillParkingExchange() {
        return new DirectExchange(
                PARKING_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue seckillParkingQueue() {
        return QueueBuilder
                .durable(PARKING_QUEUE)
                .build();
    }

    @Bean
    public Binding seckillParkingBinding(
            Queue seckillParkingQueue,
            DirectExchange seckillParkingExchange) {

        return BindingBuilder
                .bind(seckillParkingQueue)
                .to(seckillParkingExchange)
                .with(PARKING_ROUTING_KEY);
    }
}
