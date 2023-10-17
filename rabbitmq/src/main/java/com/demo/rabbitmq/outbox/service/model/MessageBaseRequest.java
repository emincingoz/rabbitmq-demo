package com.demo.rabbitmq.outbox.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageBaseRequest {
    private String uniqueTransactionId;
}
