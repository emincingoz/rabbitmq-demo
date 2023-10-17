package com.demo.rabbitmq.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublisherConfig {
    private final ApplicationContext context;

    public PublisherConfig(ApplicationContext context) {
        this.context = context;
    }

    public void demo() {
        //context.getBean()
    }
}
