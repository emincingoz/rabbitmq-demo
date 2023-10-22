package com.demo.rabbitmq.model;

import java.util.Map;

import lombok.Data;

@Data
public class ExchangeSpec {
    private String name;
    private boolean durable;
    private String type;
    private Map<String, Object> arguments;
}
