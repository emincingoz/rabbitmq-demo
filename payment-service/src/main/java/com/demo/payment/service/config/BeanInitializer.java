package com.demo.payment.service.config;

import java.util.Map;

import com.rabbitmq.client.AddressResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component("beanInitializer")
@DependsOn("addressResolverInitializer")
public class BeanInitializer {
    private static final int CONNECTION_CLOSE_TIMEOUT = 30_000;

    private final DefaultListableBeanFactory factory;
    private final SecondDemoConfig secondDemoConfig;
    private final ApplicationContext context;

    public BeanInitializer(DefaultListableBeanFactory factory, SecondDemoConfig secondDemoConfig,
                           ApplicationContext context) {
        this.factory = factory;
        this.secondDemoConfig = secondDemoConfig;
        this.context = context;
    }

    @PostConstruct
    public void connectionFactory() {
        for(Map.Entry<String, Map<String, String>> e : secondDemoConfig.getListOfMaps().entrySet()) {

            CachingConnectionFactory connectionFactory = new CachingConnectionFactory(e.getValue().get("host"),
                    Integer.parseInt(e.getValue().get("port")));
            connectionFactory.setUsername(e.getValue().get("username"));
            connectionFactory.setPassword(e.getValue().get("password"));
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CONNECTION);
            connectionFactory.setCloseTimeout(CONNECTION_CLOSE_TIMEOUT);

            AddressResolver addressResolver = (AddressResolver) context.getBean(e.getKey() + "AddressResolver");
            connectionFactory.setAddressResolver(addressResolver);

            //connectionFactory.setAddressResolver(new DnsRecordIpAddressResolver(e.getValue().get("host"), Integer
            // .parseInt(e.getValue().get("port"))));
            //addressResolver.setAddress(e.getValue().get("host"), Integer.parseInt(e.getValue().get("port")));
            //addressResolver.setSsl(false);
            //connectionFactory.setAddressResolver(addressResolver);
            //connectionFactory.setChannelListeners(Collections.singletonList(channelListener));
            /*DnsRecordIpAddressResolver addressResolver = new DnsRecordIpAddressResolver(e.getValue().get("host"),
                    Integer.parseInt(e.getValue().get("port")));
            connectionFactory.setAddressResolver(addressResolver);
            try {
                connectionFactory.setAddresses(addressResolver.getAddresses().get(0).toString());
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }*/
            //connectionFactory.setAddressResolver(new DnsSrvRecordAddressResolver());

            Object initialized = factory.initializeBean(connectionFactory, e.getKey() + "ConnectionFactory");
            //factory.setAllowBeanDefinitionOverriding(true);
            factory.autowireBeanProperties(initialized, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
            factory.registerSingleton(e.getKey() + "ConnectionFactory", initialized);

            System.out.println(e.getKey() + " -> " + e.getValue().toString());
        }
    }
}
