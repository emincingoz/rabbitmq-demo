package com.demo.payment.service.event.publisher.impl;

import com.demo.payment.service.entity.Payment;
import com.demo.payment.service.event.publisher.PaymentEventPublisher;
import com.demo.payment.service.event.publisher.model.PaymentCreateEvent;
import com.demo.payment.service.event.publisher.model.PaymentEvent;
import com.demo.rabbitmq.publisher.MessageQueueEventManager;
import com.demo.rabbitmq.outbox.enums.EventType;
import com.demo.rabbitmq.outbox.enums.ProcessType;
import com.demo.rabbitmq.publisher.model.MessageQueueInputDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentCreatePublisherImpl implements PaymentEventPublisher<Payment> {
    private final MessageQueueEventManager eventManager;

    @Value("${rabbit-config.queues.payment.paymentCreated.exchange.name}")
    private String exchange;
    @Value("${rabbit-config.queues.payment.paymentCreated.routingKey}")
    private String routingKey;

    public PaymentCreatePublisherImpl(MessageQueueEventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void createEvent(PaymentEvent<Payment> eventData) {
        log.info("PaymentCreateEvent Message will be published with payment id: {}", eventData.getData().getId());
        MessageQueueInputDTO<PaymentCreateEvent> event = new MessageQueueInputDTO<>();
        event.setEventType(getType());
        event.setProcessType(ProcessType.PAYMENT);
        PaymentCreateEvent request = getPaymentCreateEvent(eventData);
        event.setRequest(request);
        event.setExchangeName(exchange);
        event.setRoutingKey(routingKey);
        eventManager.sendEvent(event);
    }

    private static PaymentCreateEvent getPaymentCreateEvent(PaymentEvent<Payment> eventData) {
        PaymentCreateEvent request = new PaymentCreateEvent();
        request.setUniqueTransactionId(eventData.getData().getId());
        request.setId(eventData.getData().getId());
        request.setOrderId(eventData.getData().getOrderId());
        request.setAmount(eventData.getData().getAmount());
        request.setSource(eventData.getData().getSource());
        request.setCustomerId(eventData.getData().getCustomerId());
        request.setCreatedDate(eventData.getData().getCreatedDate());
        request.setUpdatedDate(eventData.getData().getUpdatedDate());
        return request;
    }

    @Override
    public EventType getType() {
        return EventType.CREATE;
    }
}
