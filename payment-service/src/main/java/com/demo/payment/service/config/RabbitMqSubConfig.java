package com.demo.payment.service.config;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqSubConfig {

    @Bean(name = "jsonMessageConverter")
    public MessageConverter jsonMessageConverter() {
        //return new Jackson2JsonMessageConverter();
        return new SerializerMessageConverter();
    }
}
