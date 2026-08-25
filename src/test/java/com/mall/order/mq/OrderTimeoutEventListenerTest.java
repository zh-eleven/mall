package com.mall.order.mq;

import com.mall.order.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutEventListenerTest {

    @Mock
    private OrderTimeoutMessagePublisher
            messagePublisher;

    @InjectMocks
    private OrderTimeoutEventListener listener;

    @Test
    void shouldPublishTimeoutMessage() {

        LocalDateTime expireTime =
                LocalDateTime.now().plusMinutes(30);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        10L,
                        expireTime
                );

        listener.handle(event);

        verify(messagePublisher).publish(
                10L,
                expireTime
        );
    }

    @Test
    void shouldNotThrowWhenRabbitMqUnavailable() {

        LocalDateTime expireTime =
                LocalDateTime.now().plusMinutes(30);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        20L,
                        expireTime
                );

        doThrow(new RuntimeException(
                "RabbitMQ unavailable"
        )).when(messagePublisher).publish(
                20L,
                expireTime
        );

        assertDoesNotThrow(() ->
                listener.handle(event)
        );

        verify(messagePublisher).publish(
                20L,
                expireTime
        );
    }
}