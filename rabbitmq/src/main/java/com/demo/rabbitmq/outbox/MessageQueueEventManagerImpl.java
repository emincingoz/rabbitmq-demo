package com.demo.rabbitmq.outbox;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.demo.rabbitmq.outbox.service.OutboxService;
import com.demo.rabbitmq.outbox.service.enums.OutboxStatus;
import com.demo.rabbitmq.outbox.service.model.MessageBaseRequest;
import com.demo.rabbitmq.outbox.service.model.MessageQueueEventDTO;
import com.demo.rabbitmq.outbox.service.model.MessageQueueInputDTO;
import com.demo.rabbitmq.outbox.service.model.MessageQueueObject;
import com.demo.rabbitmq.outbox.service.model.OutboxDTO;
import com.demo.rabbitmq.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MessageQueueEventManagerImpl implements MessageQueueEventManager {
    private static final Logger LOG = LoggerFactory.getLogger(MessageQueueEventManagerImpl.class);
    private static final Type MESSAGE_QUEUE_HEADER_MAP_TYPE = new TypeToken<HashMap<String, String>>() {}.getType();
    private static final String MQ_EXCHANGE_NAME = "mq-exchange-name";
    private static final String MQ_ROUTING_KEY = "mq-routing-key";
    private static final String MQ_AMQP_TEMPLATE_NAME = "mq-amqp-template-name";
    private final ApplicationContext context;
    private final OutboxService outboxService;
    private final MessageConverter messageConverter;
    private final ApplicationEventPublisher eventPublisher;

    public MessageQueueEventManagerImpl(ApplicationContext context, OutboxService outboxService,
                                        MessageConverter messageConverter, ApplicationEventPublisher eventPublisher) {
        this.context = context;
        this.outboxService = outboxService;
        this.messageConverter = messageConverter;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Sends event to spring boot internal
     * @param mqinputDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEvent(MessageQueueInputDTO<? extends MessageBaseRequest> mqinputDTO) {

        if (mqinputDTO.getExchangeName() == null || mqinputDTO.getRoutingKey() == null) {
            LOG.error("ExchangeName or RoutingKey is null");
            return;
        }

        if (mqinputDTO.getRequest() == null || mqinputDTO.getRequest().getUniqueTransactionId() == null) {
            LOG.error("Request data should have a unique transaction id");
            return;
        }

        // Save to outbox table
        OutboxDTO outboxDTO = generateOutboxDTO(mqinputDTO);
        outboxService.create(outboxDTO);

        // Publish event
        MessageQueueEventDTO mqEventDTO = new MessageQueueEventDTO();
        mqEventDTO.setOutboxUniqueId(outboxDTO.getUniqueId());
        eventPublisher.publishEvent(mqEventDTO);
    }

    private OutboxDTO generateOutboxDTO(MessageQueueInputDTO<? extends MessageBaseRequest> mqInputDTO) {
        OutboxDTO outboxDTO = new OutboxDTO();
        //outboxDTO.setUniqueId(UUID.randomUUID().toString());
        outboxDTO.setUniqueId(mqInputDTO.getRequest().getUniqueTransactionId());
        outboxDTO.setEventCode(mqInputDTO.getEventType().name());
        outboxDTO.setProcessCode(mqInputDTO.getProcessType().name());
        outboxDTO.setStatus(OutboxStatus.CREATED.name());
        outboxDTO.setPayload(JsonUtil.resourceToJsonText(mqInputDTO.getRequest()));
        outboxDTO.setHeader(JsonUtil.resourceToJsonText(getHeader(mqInputDTO)));
        return outboxDTO;
    }

    private Map<String, String> getHeader(MessageQueueInputDTO<? extends MessageBaseRequest> mqInputDTO) {
        Map<String, String> header = mqInputDTO.getHeader() != null ? mqInputDTO.getHeader() : new HashMap<>();
        header.put(MQ_EXCHANGE_NAME, mqInputDTO.getExchangeName());
        header.put(MQ_ROUTING_KEY, mqInputDTO.getRoutingKey());
        header.put(MQ_AMQP_TEMPLATE_NAME, mqInputDTO.getSenderTemplate());
        return header;
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

        MessageProperties properties = generateMessageProperties(header);

        Message message = messageConverter.toMessage(new MessageQueueObject<>(mqEventDTO.getOutboxUniqueId(),
                request), properties);

        AmqpTemplate amqpTemplate = (AmqpTemplate) context.getBean(header.get(MQ_AMQP_TEMPLATE_NAME));

        amqpTemplate.convertAndSend(header.get(MQ_EXCHANGE_NAME), header.get(MQ_ROUTING_KEY), message);
    }

    private static MessageProperties generateMessageProperties(Map<String, String> header) {
        MessageProperties properties = new MessageProperties();

        Map<String, String> headerMap = Optional.ofNullable(header).orElse(Collections.emptyMap()).entrySet()
                .stream()
                .filter(e -> !MQ_EXCHANGE_NAME.equals(e.getKey()) && !MQ_ROUTING_KEY.equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        headerMap.forEach(properties::setHeader);

        return properties;
    }
}
