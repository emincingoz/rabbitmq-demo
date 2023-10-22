package com.demo.rabbitmq.outbox.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rabbit-config.outbox")
public class OutboxConfigData {
    /**
     * Flag indicating whether to use an outbox table or not
     */
    private Boolean enabled;
    private String tableName;
}
