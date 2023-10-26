package com.demo.rabbitmq.publisher.impl;

import java.util.Map;
import com.demo.rabbitmq.model.RabbitConfigData;
import com.demo.rabbitmq.publisher.AbstractMessageQueueEventManager;
import com.demo.rabbitmq.publisher.model.MessageBaseRequest;
import com.demo.rabbitmq.publisher.model.MessageQueueInputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rabbit-config.outbox.enabled", havingValue = "false", matchIfMissing = true)
public class NoOutboxMessageQueueEventManagerImpl extends AbstractMessageQueueEventManager {
    private static final Logger LOG = LoggerFactory.getLogger(NoOutboxMessageQueueEventManagerImpl.class);

    public NoOutboxMessageQueueEventManagerImpl(ApplicationContext context, MessageConverter messageConverter,
                                                RabbitConfigData rabbitConfigData) {
        super(context, messageConverter, rabbitConfigData);
    }

    @Override
    public void sendEvent(MessageQueueInputDTO<? extends MessageBaseRequest> mqinputDTO) {

        LOG.info("No Outbox message queue event manager implementation worked.");

        Map<String, String> header = getHeader(mqinputDTO);
        Message message = getMessage(mqinputDTO.getRequest(), mqinputDTO.getRequest().getUniqueTransactionId(), header);
        sendMessage(header, message);

        LOG.info("Message successfully sent to queue:[exchangeName: {}, routingKey: {}] with unique transaction id: " +
                "{}", header.get(getExchangeName()), header.get(getRoutingKey()),
                mqinputDTO.getRequest().getUniqueTransactionId());
    }
}
