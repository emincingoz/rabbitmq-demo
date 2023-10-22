package com.demo.rabbitmq.publisher.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageBaseRequest {
    private String uniqueTransactionId;
}
