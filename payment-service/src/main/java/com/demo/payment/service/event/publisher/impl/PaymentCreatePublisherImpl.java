package com.demo.payment.service.event.publisher.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.demo.payment.service.entity.Payment;
import com.demo.payment.service.event.publisher.PaymentEventPublisher;
import com.demo.payment.service.event.publisher.model.PaymentCreateEvent;
import com.demo.payment.service.event.publisher.model.PaymentEvent;
import com.demo.rabbitmq.outbox.MessageQueueEventManager;
import com.demo.rabbitmq.outbox.service.enums.EventType;
import com.demo.rabbitmq.outbox.service.enums.ProcessType;
import com.demo.rabbitmq.outbox.service.model.MessageQueueInputDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentCreatePublisherImpl implements PaymentEventPublisher<Payment> {
    private final MessageQueueEventManager eventManager;

    @Value("${second-property.queues.paymentCreated.exchange.name}")
    private String exchange;
    @Value("${second-property.queues.paymentCreated.routingKey}")
    private String routingKey;

    public PaymentCreatePublisherImpl(MessageQueueEventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void createEvent(PaymentEvent<Payment> eventData) {
        String transactionId = UUID.randomUUID().toString();
        log.info("PaymentCreateEvent Message will be published with unique transaction id: {}", transactionId);
        MessageQueueInputDTO<PaymentCreateEvent> event = new MessageQueueInputDTO<>();
        event.setSenderTemplate("paymentConnectionRabbitTemplate");
        event.setEventType(getType());
        event.setProcessType(ProcessType.PAYMENT);
        PaymentCreateEvent request = new PaymentCreateEvent();
        request.setUniqueTransactionId(transactionId);
        request.setId(eventData.getData().getId());
        request.setOrderId(eventData.getData().getOrderId());
        request.setAmount(eventData.getData().getAmount());
        request.setSource(eventData.getData().getSource());
        request.setCustomerId(eventData.getData().getCustomerId());
        request.setCreatedDate(eventData.getData().getCreatedDate());
        request.setUpdatedDate(eventData.getData().getUpdatedDate());
        event.setRequest(request);
        event.setExchangeName(exchange);
        event.setRoutingKey(routingKey);
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        event.setHeader(map);
        eventManager.sendEvent(event);
    }

    @Override
    public EventType getType() {
        return EventType.CREATE;
    }
}
