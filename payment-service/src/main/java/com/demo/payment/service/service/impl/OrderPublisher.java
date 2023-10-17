package com.demo.payment.service.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.demo.payment.service.entity.Payment;
import com.demo.payment.service.event.publisher.PaymentEventPublisherProvider;
import com.demo.payment.service.event.publisher.model.PaymentEvent;
import com.demo.payment.service.service.PublisherService;
import com.demo.rabbitmq.outbox.service.enums.EventType;
import org.springframework.stereotype.Service;

@Service
public class OrderPublisher implements PublisherService {
    private final PaymentEventPublisherProvider eventPublisherProvider;

    public OrderPublisher(PaymentEventPublisherProvider eventPublisherProvider) {
        this.eventPublisherProvider = eventPublisherProvider;
    }

    @Override
    public void sendMessage() {
        Payment payment =
                Payment.builder().id(UUID.randomUUID().toString()).orderId(UUID.randomUUID().toString()).amount(
                        BigDecimal.TEN).createdDate(LocalDateTime.now()).customerId(3L).source("asdsafs").build();

        PaymentEvent<Payment> event = new PaymentEvent<>(payment);
        eventPublisherProvider.createEvent(EventType.CREATE, event);
    }
}
