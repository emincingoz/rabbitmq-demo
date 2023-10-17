package com.demo.rabbitmq.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rabbit-config.direct-login-spec")
public class DirectLoginSpec {
    private String host;
    private String port;
    private String username;
    private String password;
}
