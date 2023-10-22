package com.demo.rabbitmq.config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.demo.rabbitmq.model.QueueSpec;
import com.demo.rabbitmq.model.RabbitConfigData;
import com.demo.rabbitmq.util.EventUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
public class MessageQueueConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageQueueConfig.class);
    private final RabbitConfigData rabbitConfigData;

    public MessageQueueConfig(RabbitConfigData rabbitConfigData) {
        this.rabbitConfigData = rabbitConfigData;
    }

    protected void declareQueues(ConnectionFactory connectionFactory, String connectionGroup) {
        if (CollectionUtils.isEmpty(rabbitConfigData.getQueues()) || CollectionUtils.isEmpty(rabbitConfigData.getQueues().get(connectionGroup))) {
            return;
        }
        callQueueDeclarer(connectionFactory, connectionGroup);
    }

    private void callQueueDeclarer(ConnectionFactory connectionFactory, String connectionGroup) {
        try (Channel channel = EventUtils.getChannel(connectionFactory)) {
            rabbitConfigData.getQueues().get(connectionGroup).entrySet().stream().filter(spec -> spec.getValue().isDeclare()).forEach(spec -> callDeclareQueue(channel, spec.getValue()));
        } catch (IOException | TimeoutException e) {
            LOG.error("An error occurred while declaring queues.", e);
        }
    }

    private void callDeclareQueue(Channel channel, QueueSpec queueSpec) {
        if (!EventUtils.isValidExchangeType(queueSpec)) {
            LOG.error("Exchange type is wrong for queue. QueueName: {}, ExchangeName: {}", queueSpec.getName(),
                    queueSpec.getExchange());
            return;
        }
        try {
            declareQueue(channel, queueSpec);
        } catch (Exception e) {
            LOG.error("An error occurred while declaring queue. QueueName: {}", queueSpec.getName(), e);
        }
    }

    private void declareQueue(Channel channel, QueueSpec queueSpec) throws IOException {
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
