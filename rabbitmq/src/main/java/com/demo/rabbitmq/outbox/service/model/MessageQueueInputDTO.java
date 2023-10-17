package com.demo.rabbitmq.outbox.service.model;

import java.util.Map;

import com.demo.rabbitmq.outbox.service.enums.EventType;
import com.demo.rabbitmq.outbox.service.enums.ProcessType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageQueueInputDTO<T extends MessageBaseRequest> {
    private String senderTemplate;
    private ProcessType processType;
    private EventType eventType;
    private String exchangeName;
    private String routingKey;
    private Map<String, String> header;
    private T request;
}
