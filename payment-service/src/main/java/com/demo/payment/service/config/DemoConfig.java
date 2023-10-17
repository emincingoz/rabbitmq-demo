package com.demo.payment.service.config;

import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "property")
public class DemoConfig {
    private Map<String, String> listOfMaps;
}
