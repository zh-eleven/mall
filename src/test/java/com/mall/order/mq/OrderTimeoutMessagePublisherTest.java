package com.mall.order.mq;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutMessagePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderTimeoutMessagePublisher publisher;

    private Logger logger;

    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUpLogger() {
        logger = (Logger) LoggerFactory.getLogger(
                OrderTimeoutMessagePublisher.class
        );

        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDownLogger() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void shouldTreatReturnedMessageAsRoutingFailure() {

        CapturedPublish captured = publishAndCapture();

        ReturnedMessage returnedMessage =
                new ReturnedMessage(
                        new Message(
                                new byte[0],
                                new MessageProperties()
                        ),
                        312,
                        "NO_ROUTE",
                        RabbitOrderConfig.TIMEOUT_EXCHANGE,
                        "wrong.routing.key"
                );

        captured.correlationData()
                .setReturned(returnedMessage);

        captured.correlationData()
                .getFuture()
                .complete(
                        new CorrelationData.Confirm(
                                true,
                                null
                        )
                );

        assertTrue(logContains(
                "订单超时消息无法路由"
        ));

        assertTrue(logContains(
                "NO_ROUTE"
        ));

        assertFalse(logContains(
                "订单超时消息发送成功"
        ));
    }

    @Test
    void shouldSetPersistentDeliveryAndMessageId() {

        CapturedPublish captured = publishAndCapture();

        Message original = new Message(
                new byte[0],
                new MessageProperties()
        );

        Message processed =
                captured.messagePostProcessor()
                        .postProcessMessage(original);

        assertEquals(
                MessageDeliveryMode.PERSISTENT,
                processed.getMessageProperties()
                        .getDeliveryMode()
        );

        assertEquals(
                captured.correlationData().getId(),
                processed.getMessageProperties()
                        .getMessageId()
        );
    }

    @Test
    void shouldLogSuccessfulPublisherConfirm() {

        CapturedPublish captured = publishAndCapture();

        captured.correlationData()
                .getFuture()
                .complete(
                        new CorrelationData.Confirm(
                                true,
                                null
                        )
                );

        assertTrue(logContains(
                "订单超时消息发送成功"
        ));

        assertFalse(logContains(
                "订单超时消息发送失败"
        ));
    }

    @Test
    void shouldLogPublisherNackAsFailure() {

        CapturedPublish captured = publishAndCapture();

        captured.correlationData()
                .getFuture()
                .complete(
                        new CorrelationData.Confirm(
                                false,
                                "broker rejected"
                        )
                );

        assertTrue(logContains(
                "订单超时消息发送失败"
        ));

        assertTrue(logContains(
                "broker rejected"
        ));

        assertFalse(logContains(
                "订单超时消息发送成功"
        ));
    }

    @Test
    void shouldLogConfirmExceptionAsFailure() {

        CapturedPublish captured = publishAndCapture();

        captured.correlationData()
                .getFuture()
                .completeExceptionally(
                        new AmqpException(
                                "connection lost"
                        )
                );

        assertTrue(logContains(
                "订单超时消息发送异常"
        ));

        assertFalse(logContains(
                "订单超时消息发送成功"
        ));
    }

    private CapturedPublish publishAndCapture() {

        ArgumentCaptor<MessagePostProcessor>
                processorCaptor =
                ArgumentCaptor.forClass(
                        MessagePostProcessor.class
                );

        ArgumentCaptor<CorrelationData>
                correlationCaptor =
                ArgumentCaptor.forClass(
                        CorrelationData.class
                );

        publisher.publish(
                10L,
                LocalDateTime.now().plusMinutes(30)
        );

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitOrderConfig.TIMEOUT_EXCHANGE),
                eq(RabbitOrderConfig.TIMEOUT_ROUTING_KEY),
                any(OrderTimeoutMessage.class),
                processorCaptor.capture(),
                correlationCaptor.capture()
        );

        return new CapturedPublish(
                processorCaptor.getValue(),
                correlationCaptor.getValue()
        );
    }

    private boolean logContains(String text) {
        return appender.list.stream().anyMatch(
                event -> event
                        .getFormattedMessage()
                        .contains(text)
        );
    }

    private record CapturedPublish(
            MessagePostProcessor messagePostProcessor,
            CorrelationData correlationData
    ) {
    }
}