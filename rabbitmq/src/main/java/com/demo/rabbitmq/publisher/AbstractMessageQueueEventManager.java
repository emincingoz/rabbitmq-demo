package com.demo.rabbitmq.publisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.demo.rabbitmq.exception.MessageQueueException;
import com.demo.rabbitmq.model.QueueSpec;
import com.demo.rabbitmq.model.RabbitConfigData;
import com.demo.rabbitmq.publisher.model.MessageBaseRequest;
import com.demo.rabbitmq.publisher.model.MessageQueueInputDTO;
import com.demo.rabbitmq.publisher.model.MessageQueueObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractMessageQueueEventManager implements MessageQueueEventManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageQueueEventManager.class);
    private static final String MQ_EXCHANGE_NAME = "mq-exchange-name";
    private static final String MQ_ROUTING_KEY = "mq-routing-key";
    private static final String MQ_AMQP_TEMPLATE_NAME = "mq-amqp-template-name";
    private final ApplicationContext context;
    private final MessageConverter messageConverter;
    private final RabbitConfigData rabbitConfigData;

    public AbstractMessageQueueEventManager(ApplicationContext context, MessageConverter messageConverter,
                                            RabbitConfigData rabbitConfigData) {
        this.context = context;
        this.messageConverter = messageConverter;
        this.rabbitConfigData = rabbitConfigData;
    }

    protected abstract void sendEvent(MessageQueueInputDTO<? extends MessageBaseRequest> mqinputDTO);

    @Override
    @Transactional
    public void send(MessageQueueInputDTO<? extends MessageBaseRequest> mqinputDTO) {
        if (mqinputDTO.getExchangeName() == null || mqinputDTO.getRoutingKey() == null) {
            LOG.error("ExchangeName or RoutingKey is null");
            return;
        }

        if (mqinputDTO.getRequest() == null || mqinputDTO.getRequest().getUniqueTransactionId() == null) {
            LOG.error("Request data should have a unique transaction id");
            return;
        }

        sendEvent(mqinputDTO);
    }

    protected static MessageProperties generateMessageProperties(Map<String, String> header) {
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

    /**
     * Returns the header. The header holds the technical components used to send the message, such as exchange name
     * and routing key.
     * @param mqInputDTO
     * @return
     */
    protected Map<String, String> getHeader(MessageQueueInputDTO<? extends MessageBaseRequest> mqInputDTO) {
        Map<String, String> header = new HashMap<>();
        header.put(MQ_EXCHANGE_NAME, mqInputDTO.getExchangeName());
        header.put(MQ_ROUTING_KEY, mqInputDTO.getRoutingKey());
        header.put(MQ_AMQP_TEMPLATE_NAME, getAmqpTemplate(mqInputDTO.getRoutingKey()));
        return header;
    }

    protected Message getMessage(Object data,
                                 String uniqueTransactionId,
                                 Map<String, String> header) {
        MessageProperties properties = generateMessageProperties(header);

        Message message = messageConverter.toMessage(new MessageQueueObject<>(uniqueTransactionId,
                data), properties);
        return message;
    }

    protected void sendMessage(Map<String, String> header, Message message) {
        AmqpTemplate amqpTemplate = (AmqpTemplate) context.getBean(header.get(MQ_AMQP_TEMPLATE_NAME));
        amqpTemplate.convertAndSend(header.get(MQ_EXCHANGE_NAME), header.get(MQ_ROUTING_KEY), message);
    }

    protected String getExchangeName() {
        return MQ_EXCHANGE_NAME;
    }

    protected String getRoutingKey() {
        return MQ_ROUTING_KEY;
    }
}
