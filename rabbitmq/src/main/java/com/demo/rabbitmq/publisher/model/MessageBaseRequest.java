package com.demo.rabbitmq.publisher.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageBaseRequest implements Serializable {
    private String uniqueTransactionId;
}
