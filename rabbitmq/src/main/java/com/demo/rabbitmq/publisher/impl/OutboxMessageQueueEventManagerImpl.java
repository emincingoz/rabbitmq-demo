package com.demo.rabbitmq.publisher.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.demo.rabbitmq.publisher.AbstractMessageQueueEventManager;
import com.demo.rabbitmq.model.RabbitConfigData;
import com.demo.rabbitmq.outbox.service.OutboxService;
import com.demo.rabbitmq.outbox.enums.OutboxStatus;
import com.demo.rabbitmq.publisher.model.MessageBaseRequest;
import com.demo.rabbitmq.publisher.model.MessageQueueEventDTO;
import com.demo.rabbitmq.publisher.model.MessageQueueInputDTO;
import com.demo.rabbitmq.outbox.model.OutboxDTO;
import com.demo.rabbitmq.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(name = "rabbit-config.outbox.enabled", havingValue = "true")
public class OutboxMessageQueueEventManagerImpl extends AbstractMessageQueueEventManager {
    private static final Logger LOG = LoggerFactory.getLogger(OutboxMessageQueueEventManagerImpl.class);
    private static final Type MESSAGE_QUEUE_HEADER_MAP_TYPE = new TypeToken<HashMap<String, String>>() {}.getType();
    private final OutboxService outboxService;
    private final ApplicationEventPublisher eventPublisher;

    public OutboxMessageQueueEventManagerImpl(ApplicationContext context, OutboxService outboxService,
                                              MessageConverter messageConverter, ApplicationEventPublisher eventPublisher,
                                              RabbitConfigData rabbitConfigData) {
        super(context, messageConverter, rabbitConfigData);
        this.outboxService = outboxService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Sends event to spring boot internal. First, message data is written to the outbox table, then an event is
     * thrown to leave a message in the queue
     * @param mqinputDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEvent(MessageQueueInputDTO<? extends MessageBaseRequest> mqinputDTO) {

        LOG.info("Outbox message queue event manager implementation worked.");

        // Save to outbox table
        OutboxDTO outboxDTO = generateOutboxDTO(mqinputDTO);
        outboxService.create(outboxDTO);

        // Publish event
        MessageQueueEventDTO mqEventDTO = new MessageQueueEventDTO();
        mqEventDTO.setOutboxUniqueId(outboxDTO.getUniqueId());
        eventPublisher.publishEvent(mqEventDTO);
    }

    /**
     * OutboxDTO required to write to the outbox table is created.
     * @param mqInputDTO
     * @return
     */
    private OutboxDTO generateOutboxDTO(MessageQueueInputDTO<? extends MessageBaseRequest> mqInputDTO) {
        OutboxDTO outboxDTO = new OutboxDTO();
        outboxDTO.setUniqueId(UUID.randomUUID().toString());
        outboxDTO.setEventCode(mqInputDTO.getEventType().name());
        outboxDTO.setProcessCode(mqInputDTO.getProcessType().name());
        outboxDTO.setStatus(OutboxStatus.CREATED.name());
        outboxDTO.setPayload(JsonUtil.resourceToJsonText(mqInputDTO.getRequest()));
        outboxDTO.setHeader(JsonUtil.resourceToJsonText(getHeader(mqInputDTO)));
        return outboxDTO;
    }

    @EventListener(MessageQueueEventDTO.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void catchEvent(MessageQueueEventDTO mqEventDTO) {
        // Get with lock
        OutboxDTO outboxDTO = outboxService.getById(mqEventDTO.getOutboxUniqueId());

        // Get data from outbox
        Object request = JsonUtil.retrieveResourceFromJsonText(outboxDTO.getPayload(), Object.class);

        Map<String, String> header = JsonUtil.retrieveResourceFromJsonText(outboxDTO.getHeader(),
                MESSAGE_QUEUE_HEADER_MAP_TYPE);

        outboxService.updateStatus(outboxDTO.getUniqueId(), OutboxStatus.SENT);

        Message message = getMessage(request, mqEventDTO.getOutboxUniqueId(), header);
        sendMessage(header, message);

        LOG.info("Message successfully sent to queue:[exchangeName: {}, routingKey: {}] with unique transaction id: " +
                "{}", header.get(getExchangeName()), header.get(getRoutingKey()), mqEventDTO.getOutboxUniqueId());

    }
}
