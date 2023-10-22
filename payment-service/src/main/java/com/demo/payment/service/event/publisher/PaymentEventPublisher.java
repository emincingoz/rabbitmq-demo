package com.demo.payment.service.event.publisher;

import com.demo.payment.service.event.publisher.model.PaymentEvent;
import com.demo.rabbitmq.outbox.enums.EventType;

public interface PaymentEventPublisher<T> {
    void createEvent(PaymentEvent<T> event);
    EventType getType();
}
