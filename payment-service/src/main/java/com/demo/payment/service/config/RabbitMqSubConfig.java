package com.demo.payment.service.config;

import com.rabbitmq.client.AddressResolver;
import com.rabbitmq.client.DnsRecordIpAddressResolver;
import com.rabbitmq.client.DnsSrvRecordAddressResolver;
import org.springframework.amqp.rabbit.connection.ChannelListener;
import org.springframework.amqp.rabbit.connection.CompositeChannelListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqSubConfig {

    /*@Bean
    public AddressResolver addressResolver() {
        return new DnsRecordIpAddressResolver();
    }*/

    @Bean
    public ChannelListener channelListener() {
        return new CompositeChannelListener();
    }

    @Bean(name = "jsonMessageConverter")
    public MessageConverter jsonMessageConverter() {
        //return new Jackson2JsonMessageConverter();
        return new SerializerMessageConverter();
    }
}
