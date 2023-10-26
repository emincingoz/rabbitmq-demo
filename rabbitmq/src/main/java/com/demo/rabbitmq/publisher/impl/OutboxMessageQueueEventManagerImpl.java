package com.demo.rabbitmq.publisher.impl;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.demo.rabbitmq.publisher.MessageQueueEventManager;
import com.demo.rabbitmq.model.QueueSpec;
import com.demo.rabbitmq.model.RabbitConfigData;
import com.demo.rabbitmq.exception.MessageQueueException;
import com.demo.rabbitmq.outbox.service.OutboxService;
import com.demo.rabbitmq.outbox.enums.OutboxStatus;
import com.demo.rabbitmq.publisher.model.MessageBaseRequest;
import com.demo.rabbitmq.publisher.model.MessageQueueEventDTO;
import com.demo.rabbitmq.publisher.model.MessageQueueInputDTO;
import com.demo.rabbitmq.publisher.model.MessageQueueObject;
import com.demo.rabbitmq.outbox.model.OutboxDTO;
import com.demo.rabbitmq.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
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
public class OutboxMessageQueueEventManagerImpl implements MessageQueueEventManager {
    private static final Logger LOG = LoggerFactory.getLogger(OutboxMessageQueueEventManagerImpl.class);
    private static final Type MESSAGE_QUEUE_HEADER_MAP_TYPE = new TypeToken<HashMap<String, String>>() {}.getType();
    private static final String MQ_EXCHANGE_NAME = "mq-exchange-name";
    private static final String MQ_ROUTING_KEY = "mq-routing-key";
    private static final String MQ_AMQP_TEMPLATE_NAME = "mq-amqp-template-name";
    private final ApplicationContext context;
    private final OutboxService outboxService;
    private final MessageConverter messageConverter;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitConfigData rabbitConfigData;

    public OutboxMessageQueueEventManagerImpl(ApplicationContext context, OutboxService outboxService,
                                              MessageConverter messageConverter, ApplicationEventPublisher eventPublisher,
                                              RabbitConfigData rabbitConfigData) {
        this.context = context;
        this.outboxService = outboxService;
        this.messageConverter = messageConverter;
        this.eventPublisher = eventPublisher;
        this.rabbitConfigData = rabbitConfigData;
    }

    /**
     * Sends event to spring boot internal. First, message data is written to the outbox table, then an event is
     * thrown to leave a message in the queue
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

        LOG.debug("Outbox message queue event manager implementation worked.");

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

    /**
     * Returns the header. The header holds the technical components used to send the message, such as exchange name
     * and routing key.
     * @param mqInputDTO
     * @return
     */
    private Map<String, String> getHeader(MessageQueueInputDTO<? extends MessageBaseRequest> mqInputDTO) {
        Map<String, String> header = new HashMap<>();
        header.put(MQ_EXCHANGE_NAME, mqInputDTO.getExchangeName());
        header.put(MQ_ROUTING_KEY, mqInputDTO.getRoutingKey());
        header.put(MQ_AMQP_TEMPLATE_NAME, getAmqpTemplate(mqInputDTO.getRoutingKey()));
        return header;
    }

    /**
     * Returns the bean name of template name that will be used to send the message. The connection name to which the
     * message belongs is found by using the roting key information to which the message was sent. The name rabbit
     * template bean is found by adding the RabbitTemplate suffix to the end of the connection name as required by business.
     * @param routingKey
     * @return
     */
    private String getAmqpTemplate(String routingKey) {
        for (Map.Entry<String, Map<String, QueueSpec>> queueSpec : rabbitConfigData.getQueues().entrySet()) {
            for (Map.Entry<String, QueueSpec> queueConfig : queueSpec.getValue().entrySet()) {
                if (routingKey.equals(queueConfig.getValue().getRoutingKey())) {
                    // RabbitTemplate suffix is added to the end of amqp templates at MessagePublisherConfig class
                    String amqpTemplate = STR."\{queueSpec.getKey()}RabbitTemplate";
                    LOG.info("The amqp template name {} was found for the routing key: {}", amqpTemplate, routingKey);
                    return amqpTemplate;
                }
            }
        }
        LOG.error("Any amqp template name can not be found for the routing key: {}", routingKey);
        throw new MessageQueueException("Any amqp template name can not be found for the routing key: " + routingKey);
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
                .filter(e -> !MQ_EXCHANGE_NAME.equals(e.getKey()) &&
                        !MQ_ROUTING_KEY.equals(e.getKey()) &&
                        !MQ_AMQP_TEMPLATE_NAME.equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        headerMap.forEach(properties::setHeader);

        return properties;
    }
}
