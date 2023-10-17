package com.demo.payment.service.config;

import java.util.Map;

import com.demo.payment.service.config.model.QueueSpec;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "second-property")
public class SecondDemoConfig {
    private Map<String, Map<String, String>> listOfMaps;
    private Map<String, QueueSpec> queues;
}
