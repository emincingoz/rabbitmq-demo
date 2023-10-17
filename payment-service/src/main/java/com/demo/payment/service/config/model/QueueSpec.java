package com.demo.payment.service.config.model;

import java.util.Map;

import lombok.Data;

@Data
public class QueueSpec {
    private boolean declare;
    private String name;
    private boolean durable;
    private String routingKey;
    private Map<String, Object> arguments;
    private ExchangeSpec exchange;
}
