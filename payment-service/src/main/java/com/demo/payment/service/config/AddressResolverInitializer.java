package com.demo.payment.service.config;

import java.util.Map;

import com.rabbitmq.client.AddressResolver;
import com.rabbitmq.client.DnsRecordIpAddressResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

@Component("addressResolverInitializer")
public class AddressResolverInitializer {

    private final DefaultListableBeanFactory factory;
    private final SecondDemoConfig secondDemoConfig;

    public AddressResolverInitializer(DefaultListableBeanFactory factory, SecondDemoConfig secondDemoConfig) {
        this.factory = factory;
        this.secondDemoConfig = secondDemoConfig;
    }

    @PostConstruct
    public void addressResolverInitializer() {
        for(Map.Entry<String, Map<String, String>> e : secondDemoConfig.getListOfMaps().entrySet()) {

            AddressResolver addressResolver = new DnsRecordIpAddressResolver(e.getValue().get("host"),
                    Integer.parseInt(e.getValue().get("port")));

            Object initialized = factory.initializeBean(addressResolver, e.getKey() + "AddressResolver");
            factory.autowireBeanProperties(initialized, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
            factory.registerSingleton(e.getKey() + "AddressResolver", initialized);

            System.out.println(e.getKey() + " -> " + e.getValue().toString());
        }
    }
}
