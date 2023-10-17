package com.demo.payment.service.event.publisher;

import java.util.List;
import java.util.Optional;

import com.demo.payment.service.event.publisher.model.PaymentEvent;
import com.demo.rabbitmq.outbox.service.enums.EventType;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisherProviderImpl implements PaymentEventPublisherProvider {
    private final List<PaymentEventPublisher<?>> eventPublisherList;

    public PaymentEventPublisherProviderImpl(List<PaymentEventPublisher<?>> eventPublisherList) {
        this.eventPublisherList = eventPublisherList;
    }

    @Override
    public void createEvent(EventType eventType, PaymentEvent paymentEvent) {
        getEventPublisher(eventType).ifPresent(eventPublisher -> eventPublisher.createEvent(paymentEvent));
    }

    private Optional<PaymentEventPublisher<?>> getEventPublisher(EventType eventType) {
        return eventPublisherList.stream().filter(e -> e.getType() == eventType).findFirst();
    }
}
