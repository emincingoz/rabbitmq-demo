package com.demo.rabbitmq.model;

import lombok.Data;

@Data
public class ConnectionSpec {
    private String host;
    private Integer port;
    private String username;
    private String password;
}
