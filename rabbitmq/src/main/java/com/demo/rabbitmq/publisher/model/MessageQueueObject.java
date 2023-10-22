package com.demo.rabbitmq.publisher.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueueObject<T> implements Serializable {
    private String messageId;
    private T jsonObject;
}
