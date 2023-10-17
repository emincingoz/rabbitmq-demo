package com.demo.payment.service.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.demo.payment.service.config.model.QueueSpec;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("beanInitializer")
public class MessagePublisherConfig {
    private static final int CONNECTION_CLOSE_TIMEOUT = 30_000;

    private final ApplicationContext context;
    private final DefaultListableBeanFactory factory;
    private final MessageConverter messageConverter;
    private final SecondDemoConfig secondDemoConfig;

    private Map<String, QueueSpec> queues;

    public MessagePublisherConfig(ApplicationContext context, DefaultListableBeanFactory factory,
                                  @Qualifier("jsonMessageConverter") MessageConverter messageConverter, SecondDemoConfig secondDemoConfig) {
        this.context = context;
        this.factory = factory;
        this.messageConverter = messageConverter;
        this.secondDemoConfig = secondDemoConfig;
    }

    /*@PostConstruct
    public void checkConnectionFactoriesUp() {
        Object orderConnectionFactory = context.getBean("orderConnectionFactory");
        ConnectionFactory paymentConnectionFactory = (CachingConnectionFactory) context.getBean("paymentConnectionFactory");
        System.out.println("Checking Connection Factories are Up Currently!");
    }*/

    @PostConstruct
    public void amqpTemplateGenerator() {
        for(Map.Entry<String, Map<String, String>> e : secondDemoConfig.getListOfMaps().entrySet()) {

            ConnectionFactory connectionFactory = (ConnectionFactory) context.getBean(e.getKey() + "ConnectionFactory");

            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setMessageConverter(messageConverter);
            rabbitTemplate.setChannelTransacted(false);

            Object initialized = factory.initializeBean(rabbitTemplate, e.getKey() + "RabbitTemplate");
            factory.autowireBeanProperties(initialized, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
            factory.registerSingleton(e.getKey() + "RabbitTemplate", initialized);

            declareQueues(connectionFactory);
            System.out.println(e.getKey() + " -> " + e.getValue().toString());
        }
    }

    private void declareQueues(ConnectionFactory connectionFactory) {
        if (secondDemoConfig.getQueues() == null) {
            return;
        }

        try (Channel channel = EventUtils.getChannel(connectionFactory)) {
            secondDemoConfig.getQueues().entrySet().stream().filter(spec -> spec.getValue().isDeclare()).forEach(spec -> declareQueue(channel, spec.getValue()));
        } catch (IOException | TimeoutException e) {

        }
    }

    private void declareQueue(Channel channel, QueueSpec queueSpec) {
        if (!EventUtils.isValidExchangeType(queueSpec)) {
            // Exchange type is wrong for queue. QueueName: {}, ExchangeName: {},,,,,
            System.out.println("Exchange type is wrong for queue. QueueName: " + queueSpec.getName() + ", " +
                            "ExchangeName: " + queueSpec.getExchange().getName());
            return;
        }

        try {
            callQueueDeclare(channel, queueSpec);
        } catch (Exception e) {

        }
    }

    private void callQueueDeclare(Channel channel, QueueSpec queueSpec) throws IOException {
        try {
            AMQP.Queue.DeclareOk response = channel.queueDeclarePassive(queueSpec.getName());
            System.out.println("Queue already desclared. QueueName: " + queueSpec.getName());
        } catch (IOException e) {
            channel.exchangeDeclare(queueSpec.getExchange().getName(), queueSpec.getExchange().getType(),
                    queueSpec.getExchange().isDurable());

            channel.queueDeclare(queueSpec.getName(), queueSpec.isDurable(), false, false, queueSpec.getArguments());

            channel.queueBind(queueSpec.getName(), queueSpec.getExchange().getName(), queueSpec.getRoutingKey());

            System.out.println("asdasfds");
        }
    }
}
