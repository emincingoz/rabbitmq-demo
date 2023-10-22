package com.demo.rabbitmq.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.demo.rabbitmq.model.ConnectionSpec;
import com.demo.rabbitmq.model.QueueSpec;
import com.demo.rabbitmq.util.BeanFactory;
import com.demo.rabbitmq.util.EventUtils;
import com.demo.rabbitmq.model.RabbitConfigData;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.CollectionUtils;

@Configuration("messagePublisherConfig")
@DependsOn("connectionFactoryInitializer")
public class MessagePublisherConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessagePublisherConfig.class);
    private final ApplicationContext context;
    private final BeanFactory beanFactory;
    private final MessageConverter messageConverter;
    private final RabbitConfigData rabbitConfigData;

    public MessagePublisherConfig(ApplicationContext context, BeanFactory beanFactory,
                                  @Qualifier("messageConverter") MessageConverter messageConverter,
                                  RabbitConfigData rabbitConfigData) {
        this.context = context;
        this.beanFactory = beanFactory;
        this.messageConverter = messageConverter;
        this.rabbitConfigData = rabbitConfigData;
    }

    @PostConstruct
    public void amqpTemplateGenerator() {

        for(Map.Entry<String, ConnectionSpec> e : rabbitConfigData.getConnections().entrySet()) {

            Map<String, QueueSpec> queues = rabbitConfigData.getQueues().get(e.getKey());

            if (CollectionUtils.isEmpty(queues)) {
                continue;
            }

            String rabbitTemplateBeanName = e.getKey() + "RabbitTemplate";

            ConnectionFactory connectionFactory = (ConnectionFactory) context.getBean(e.getKey() + "ConnectionFactory");

            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setMessageConverter(messageConverter);
            rabbitTemplate.setChannelTransacted(false);

            beanFactory.initializeBean(rabbitTemplate, rabbitTemplateBeanName);
            declareQueues(connectionFactory, e.getKey());
        }
    }

    private void declareQueues(ConnectionFactory connectionFactory, String connectionGroup) {
        if (CollectionUtils.isEmpty(rabbitConfigData.getQueues()) || CollectionUtils.isEmpty(rabbitConfigData.getQueues().get(connectionGroup))) {
            return;
        }

        try (Channel channel = EventUtils.getChannel(connectionFactory)) {
            rabbitConfigData.getQueues().get(connectionGroup).entrySet().stream().filter(spec -> spec.getValue().isDeclare()).forEach(spec -> declareQueue(channel, spec.getValue()));
        } catch (IOException | TimeoutException e) {
            LOG.error("An error occurred while declaring queues.", e);
        }
    }

    private void declareQueue(Channel channel, QueueSpec queueSpec) {
        if (!EventUtils.isValidExchangeType(queueSpec)) {
            LOG.error("Exchange type is wrong for queue. QueueName: {}, ExchangeName: {}", queueSpec.getName(),
                    queueSpec.getExchange());
            return;
        }
        try {
            callQueueDeclare(channel, queueSpec);
        } catch (Exception e) {
            LOG.error("An error occurred while declaring queue. QueueName: {}", queueSpec.getName(), e);
        }
    }

    private void callQueueDeclare(Channel channel, QueueSpec queueSpec) throws IOException {
        try {
            AMQP.Queue.DeclareOk response = channel.queueDeclarePassive(queueSpec.getName());
            LOG.info("Queue already declared. QueueName: {}", queueSpec.getName());
            LOG.info("QueueName: {}. Ready message count: {}, Consumer count {}", queueSpec.getName(),
                    response.getMessageCount(), response.getConsumerCount());
        } catch (IOException e) {
            LOG.error("Queue not declared. QueueName: {}", queueSpec.getName());

            channel.exchangeDeclare(queueSpec.getExchange().getName(), queueSpec.getExchange().getType(),
                    queueSpec.getExchange().isDurable());
            LOG.info("Exchange declared. QueueName: {}, ExchangeName: {}", queueSpec.getName(),
                    queueSpec.getExchange().getName());

            channel.queueDeclare(queueSpec.getName(), queueSpec.isDurable(), false, false, queueSpec.getArguments());
            LOG.info("Queue declared. QueueName: {}, ExchangeName: {}", queueSpec.getName(),queueSpec.getExchange().getName());

            channel.queueBind(queueSpec.getName(), queueSpec.getExchange().getName(), queueSpec.getRoutingKey());
            LOG.info("{} queue bound to {} exchange by {} routing key", queueSpec.getName(),
                    queueSpec.getExchange().getName(), queueSpec.getRoutingKey());
        }
    }
}