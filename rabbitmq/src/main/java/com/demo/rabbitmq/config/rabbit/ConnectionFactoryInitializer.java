package com.demo.rabbitmq.config.rabbit;

import java.util.Map;

import com.demo.rabbitmq.BeanFactory;
import com.demo.rabbitmq.config.rabbit.model.ConnectionSpec;
import com.demo.rabbitmq.config.rabbit.model.RabbitConfigData;
import com.rabbitmq.client.AddressResolver;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component("connectionFactoryInitializer")
@DependsOn("addressResolverInitializer")
public class ConnectionFactoryInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFactoryInitializer.class);
    private static final int CONNECTION_CLOSE_TIMEOUT = 30_000;
    private final ApplicationContext context;
    private final BeanFactory beanFactory;
    private final RabbitConfigData rabbitConfigData;

    public ConnectionFactoryInitializer(ApplicationContext context, BeanFactory beanFactory,
                                        RabbitConfigData rabbitConfigData) {
        this.context = context;
        this.rabbitConfigData = rabbitConfigData;
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void connectionFactory() {
        for(Map.Entry<String, ConnectionSpec> e : rabbitConfigData.getConnections().entrySet()) {

            String connectionFactoryBeanName = e.getKey() + "ConnectionFactory";
            ConnectionSpec connectionSpec = e.getValue();

            CachingConnectionFactory connectionFactory = new CachingConnectionFactory(connectionSpec.getHost(),
                    connectionSpec.getPort());
            connectionFactory.setUsername(connectionSpec.getUsername());
            connectionFactory.setPassword(connectionSpec.getPassword());
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CONNECTION);
            connectionFactory.setCloseTimeout(CONNECTION_CLOSE_TIMEOUT);

            AddressResolver addressResolver = (AddressResolver) context.getBean(e.getKey() + "AddressResolver");
            connectionFactory.setAddressResolver(addressResolver);

            beanFactory.initializeBean(connectionFactory, connectionFactoryBeanName);
            LOG.info("ConnectionFactory bean initialized with name: {}", connectionFactoryBeanName);
        }
    }
}