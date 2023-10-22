package com.demo.rabbitmq.config;

import java.util.Map;

import com.demo.rabbitmq.model.ConnectionSpec;
import com.demo.rabbitmq.util.BeanFactory;
import com.demo.rabbitmq.model.RabbitConfigData;
import com.rabbitmq.client.AddressResolver;
import com.rabbitmq.client.DnsRecordIpAddressResolver;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("addressResolverInitializer")
public class AddressResolverInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(AddressResolverInitializer.class);
    private final RabbitConfigData rabbitConfigData;
    private final BeanFactory beanFactory;

    public AddressResolverInitializer(RabbitConfigData rabbitConfigData,
                                      BeanFactory beanFactory) {
        this.rabbitConfigData = rabbitConfigData;
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void addressResolverInitializer() {

        for(Map.Entry<String, ConnectionSpec> e : rabbitConfigData.getConnections().entrySet()) {

            String addressResolverBeanName = e.getKey() + "AddressResolver";
            ConnectionSpec connectionSpec = e.getValue();
            if (connectionSpec.getDeclare()) {
                AddressResolver addressResolver = new DnsRecordIpAddressResolver(connectionSpec.getHost(),
                        connectionSpec.getPort());
                beanFactory.initializeBean(addressResolver, addressResolverBeanName);
                LOG.info("AddressResolver bean initialized with bean name: {}", addressResolverBeanName);
            }
        }
    }
}
