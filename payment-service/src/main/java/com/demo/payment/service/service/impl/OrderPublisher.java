package com.demo.payment.service.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.demo.payment.service.entity.Payment;
import com.demo.payment.service.service.PublisherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderPublisher implements PublisherService {

    @Value("${second-property.queues.paymentCreated.exchange.name}")
    private String exchange;
    @Value("${second-property.queues.paymentCreated.routingKey}")
    private String routingKey;

    private final AmqpTemplate amqpTemplate;

    public OrderPublisher(@Qualifier("orderConnectionRabbitTemplate") AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void sendMessage() {
        Payment payment =
                Payment.builder().id(UUID.randomUUID().toString()).orderId(UUID.randomUUID().toString()).amount(
                        BigDecimal.TEN).createdDate(LocalDateTime.now()).customerId(3L).source("asdsafs").build();
        String message = payment.toString();
        /*ObjectMapper objectMapper = new ObjectMapper();
        try {
            message = objectMapper.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }*/
        amqpTemplate.convertAndSend(exchange, routingKey, message);

        System.out.println("dgdfhgfh");
    }
}
