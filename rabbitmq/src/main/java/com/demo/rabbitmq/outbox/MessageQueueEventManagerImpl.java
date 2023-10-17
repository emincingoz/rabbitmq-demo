package com.demo.rabbitmq.outbox;

import com.demo.rabbitmq.outbox.service.model.MessageBaseRequest;
import com.demo.rabbitmq.outbox.service.model.MessageQueueInputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MessageQueueEventManagerImpl implements MessageQueueEventManager {
    private static final Logger LOG = LoggerFactory.getLogger(MessageQueueEventManagerImpl.class);
    private final ApplicationContext context;

    public MessageQueueEventManagerImpl(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void sendEvent(MessageQueueInputDTO<? extends MessageBaseRequest> messageQueueInputDTO) {
        AmqpTemplate amqpTemplate = (AmqpTemplate) context.getBean(messageQueueInputDTO.getSenderTemplate());

        amqpTemplate.convertAndSend(messageQueueInputDTO.getExchangeName(), messageQueueInputDTO.getRoutingKey(),
                messageQueueInputDTO.getRequest().toString());
        LOG.info("Message sent");
    }
}
