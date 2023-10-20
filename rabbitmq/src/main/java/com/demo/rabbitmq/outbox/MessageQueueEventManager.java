package com.demo.rabbitmq.outbox;

import com.demo.rabbitmq.outbox.service.model.MessageBaseRequest;
import com.demo.rabbitmq.outbox.service.model.MessageQueueInputDTO;

public interface MessageQueueEventManager {

    void sendEvent(MessageQueueInputDTO<? extends MessageBaseRequest> mqinputDTO);
}
