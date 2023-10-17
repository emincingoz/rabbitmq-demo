package com.demo.rabbitmq.outbox.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueueObject<T> {
    private String messageId;
    private T jsonObject;
}
