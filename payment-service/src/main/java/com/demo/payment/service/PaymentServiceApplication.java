package com.demo.payment.service;

import static java.lang.StringTemplate.STR;

import java.util.Map;

import com.demo.rabbitmq.config.rabbit.model.ConnectionSpec;
import com.demo.rabbitmq.config.rabbit.model.QueueSpec;
import com.demo.rabbitmq.config.rabbit.model.RabbitConfigData;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;

@DependsOn("messagePublisherConfig")
@ComponentScan(basePackages = {"com.demo"})
@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
public class PaymentServiceApplication implements CommandLineRunner {
    private final RabbitConfigData rabbitConfigData;

    public PaymentServiceApplication(RabbitConfigData rabbitConfigData) {
        this.rabbitConfigData = rabbitConfigData;
    }

    public static void main(String ... args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Map<String, ConnectionSpec> connections = rabbitConfigData.getConnections();
        Map<String, Map<String, QueueSpec>> queues = rabbitConfigData.getQueues();
        System.out.println(rabbitConfigData.getConnections().toString());
        System.out.println(rabbitConfigData.getQueues().toString());

        String sql = STR."""
                SELECT UNIQUE_ID, VERSION, CREATED_DATE, UPDATED_DATE, STATUS, PROCESS_CODE, EVENT_CODE, HEADER, PAYLOAD
                FROM \{rabbitConfigData.getConnections().toString()}
                WHERE UNIQUE_ID = ?
                FOR UPDATE WAIT 2
                """;

        System.out.println("Hello: " + sql);
    }
}
