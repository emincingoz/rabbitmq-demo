package com.demo.rabbitmq.config;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConverterConfig {

    @Bean(name = "messageConverter")
    public MessageConverter messageConverter() {
        //return new Jackson2JsonMessageConverter();
        return new SerializerMessageConverter();
    }
}
