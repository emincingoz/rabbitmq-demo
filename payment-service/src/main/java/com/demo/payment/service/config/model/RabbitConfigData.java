package com.demo.payment.service.config.model;

import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rabbit-config")
public class RabbitConfigData {
    private Map<String, ConnectionSpec> connections;
    private Map<String, Map<String, QueueSpec>> queues;
}
