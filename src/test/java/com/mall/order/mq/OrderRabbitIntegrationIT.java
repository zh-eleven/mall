package com.mall.order.mq;

import com.mall.order.config.OrderProperties;
import com.mall.order.service.OrderCancellationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(
        classes = OrderRabbitIntegrationIT.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "order.timeout-minutes=30",

                "spring.rabbitmq.publisher-confirm-type=correlated",
                "spring.rabbitmq.publisher-returns=true",
                "spring.rabbitmq.template.mandatory=true",

                "spring.rabbitmq.listener.simple.acknowledge-mode=auto",
                "spring.rabbitmq.listener.simple.default-requeue-rejected=false",

                "spring.rabbitmq.listener.simple.retry.enabled=true",
                "spring.rabbitmq.listener.simple.retry.max-attempts=3",
                "spring.rabbitmq.listener.simple.retry.initial-interval=100ms",
                "spring.rabbitmq.listener.simple.retry.multiplier=1",
                "spring.rabbitmq.listener.simple.retry.max-interval=100ms"
        }
)
class OrderRabbitIntegrationIT {

    @Container
    static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer(
                    DockerImageName.parse(
                            "rabbitmq:4-management"
                    )
            )
                    .withEnv(
                            "RABBITMQ_DEFAULT_USER",
                            "mall"
                    )
                    .withEnv(
                            "RABBITMQ_DEFAULT_PASS",
                            "mall"
                    );

    @DynamicPropertySource
    static void configureRabbitMq(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.rabbitmq.host",
                RABBITMQ::getHost
        );

        registry.add(
                "spring.rabbitmq.port",
                RABBITMQ::getAmqpPort
        );

        registry.add(
                "spring.rabbitmq.username",
                () -> "mall"
        );

        registry.add(
                "spring.rabbitmq.password",
                () -> "mall"
        );

        registry.add(
                "spring.rabbitmq.virtual-host",
                () -> "/"
        );
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @MockitoBean
    private OrderCancellationService
            orderCancellationService;

    @BeforeEach
    void clearQueuesAndMocks() {

        amqpAdmin.purgeQueue(
                RabbitOrderConfig.TIMEOUT_QUEUE,
                false
        );

        amqpAdmin.purgeQueue(
                RabbitOrderConfig.CANCEL_QUEUE,
                false
        );

        amqpAdmin.purgeQueue(
                RabbitOrderConfig.DEAD_QUEUE,
                false
        );

        reset(orderCancellationService);
    }

    /**
     * 验证完整链路：
     *
     * timeout exchange
     * -> timeout queue
     * -> TTL 到期
     * -> cancel exchange
     * -> cancel queue
     * -> listener
     */
    @Test
    void expiredMessageShouldReachCancellationListener() {

        when(orderCancellationService
                .cancelTimedOutOrder(
                        eq(101L),
                        any(LocalDateTime.class)
                ))
                .thenReturn(true);

        publishWithShortTtl(101L);

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() ->
                        verify(orderCancellationService)
                                .cancelTimedOutOrder(
                                        eq(101L),
                                        any(LocalDateTime.class)
                                )
                );
    }

    /**
     * 验证消费者异常时：
     *
     * 重试三次
     * -> 拒绝消息
     * -> dead exchange
     * -> dead queue
     */
    @Test
    void failedMessageShouldRetryAndEnterDeadQueue() {

        when(orderCancellationService
                .cancelTimedOutOrder(
                        eq(102L),
                        any(LocalDateTime.class)
                ))
                .thenThrow(
                        new IllegalStateException(
                                "模拟订单取消失败"
                        )
                );

        publishWithShortTtl(102L);

        AtomicReference<Message> deadMessage =
                new AtomicReference<>();

        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> {

                    Message message =
                            rabbitTemplate.receive(
                                    RabbitOrderConfig.DEAD_QUEUE
                            );

                    if (message == null) {
                        return false;
                    }

                    deadMessage.set(message);
                    return true;
                });

        assertNotNull(deadMessage.get());

        verify(
                orderCancellationService,
                times(3)
        ).cancelTimedOutOrder(
                eq(102L),
                any(LocalDateTime.class)
        );
    }

    /**
     * 生产队列 TTL 是30分钟。
     * 测试消息额外设置500毫秒 TTL，
     * RabbitMQ 会采用更短的过期时间。
     */
    private void publishWithShortTtl(Long orderId) {

        OrderTimeoutMessage message =
                new OrderTimeoutMessage(
                        orderId,
                        LocalDateTime.now()
                                .plusSeconds(1)
                );

        rabbitTemplate.convertAndSend(
                RabbitOrderConfig.TIMEOUT_EXCHANGE,
                RabbitOrderConfig.TIMEOUT_ROUTING_KEY,
                message,
                rabbitMessage -> {

                    rabbitMessage
                            .getMessageProperties()
                            .setExpiration("500");

                    rabbitMessage
                            .getMessageProperties()
                            .setDeliveryMode(
                                    MessageDeliveryMode.PERSISTENT
                            );

                    return rabbitMessage;
                }
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = {
                    DataSourceAutoConfiguration.class,
                    RedisAutoConfiguration.class
            }
    )
    @EnableConfigurationProperties(
            OrderProperties.class
    )
    @Import({
            RabbitOrderConfig.class,
            OrderTimeoutMessageListener.class
    })
    static class TestApplication {
    }
}