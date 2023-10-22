package com.demo.rabbitmq.publisher.model;

import com.demo.rabbitmq.outbox.enums.EventType;
import com.demo.rabbitmq.outbox.enums.ProcessType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageQueueInputDTO<T extends MessageBaseRequest> {
    private ProcessType processType;
    private EventType eventType;
    private String exchangeName;
    private String routingKey;
    private T request;
}
