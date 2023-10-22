package com.demo.rabbitmq.config;

import java.util.Map;

import com.demo.rabbitmq.model.ConnectionSpec;
import com.demo.rabbitmq.model.QueueSpec;
import com.demo.rabbitmq.util.BeanFactory;
import com.demo.rabbitmq.model.RabbitConfigData;
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
    private final MessageQueueConfig messageQueueConfig;

    public MessagePublisherConfig(ApplicationContext context, BeanFactory beanFactory,
                                  @Qualifier("messageConverter") MessageConverter messageConverter,
                                  RabbitConfigData rabbitConfigData, MessageQueueConfig messageQueueConfig) {
        this.context = context;
        this.beanFactory = beanFactory;
        this.messageConverter = messageConverter;
        this.rabbitConfigData = rabbitConfigData;
        this.messageQueueConfig = messageQueueConfig;
    }

    @PostConstruct
    public void createAmqpTemplates() {

        for(Map.Entry<String, ConnectionSpec> queueConfig : rabbitConfigData.getConnections().entrySet()) {
            Map<String, QueueSpec> queues = rabbitConfigData.getQueues().get(queueConfig.getKey());
            if (!queueConfig.getValue().getDeclare() || CollectionUtils.isEmpty(queues)) {
                continue;
            }
            createAmqpTemplate(queueConfig.getKey());
        }
    }

    private void createAmqpTemplate(String connectionName) {
        ConnectionFactory connectionFactory = getConnectionFactory(connectionName);
        RabbitTemplate rabbitTemplate = getRabbitTemplate(connectionFactory);
        if (rabbitTemplate != null) {
            beanFactory.initializeBean(rabbitTemplate, connectionName + "RabbitTemplate");
            messageQueueConfig.declareQueues(rabbitTemplate.getConnectionFactory(), connectionName);
        }
    }

    private RabbitTemplate getRabbitTemplate(ConnectionFactory connectionFactory) {
        if (connectionFactory != null) {
            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setMessageConverter(messageConverter);
            rabbitTemplate.setChannelTransacted(false);
            return rabbitTemplate;
        }
        return null;
    }

    private ConnectionFactory getConnectionFactory(String connectionSpecKey) {
        String connectionFactoryBeanName = connectionSpecKey + "ConnectionFactory";
        try {
            return (ConnectionFactory) context.getBean(connectionFactoryBeanName);
        } catch (RuntimeException e) {
            LOG.error("Could not found any connection factory bean with name {}",
                    connectionFactoryBeanName);
        }
        return null;
    }
}