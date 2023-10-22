package com.demo.rabbitmq.publisher;

import com.demo.rabbitmq.publisher.model.MessageBaseRequest;
import com.demo.rabbitmq.publisher.model.MessageQueueInputDTO;

public interface MessageQueueEventManager {

    void sendEvent(MessageQueueInputDTO<? extends MessageBaseRequest> mqinputDTO);
}
