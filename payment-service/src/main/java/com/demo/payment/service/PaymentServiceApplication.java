package com.demo.payment.service;

import java.util.Map;

import com.demo.payment.service.config.DemoConfig;
import com.demo.payment.service.config.SecondDemoConfig;
import com.demo.payment.service.config.model.ConnectionSpec;
import com.demo.payment.service.config.model.QueueSpec;
import com.demo.payment.service.config.model.RabbitConfigData;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;

@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
public class PaymentServiceApplication implements CommandLineRunner {

    private final DemoConfig demoConfig;
    private final SecondDemoConfig secondDemoConfig;
    private final RabbitConfigData rabbitConfigData;

    public PaymentServiceApplication(DemoConfig demoConfig, SecondDemoConfig secondDemoConfig,
                                     RabbitConfigData rabbitConfigData) {
        this.demoConfig = demoConfig;
        this.secondDemoConfig = secondDemoConfig;
        this.rabbitConfigData = rabbitConfigData;
    }

    public static void main(String ... args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Map<String, String> configs = demoConfig.getListOfMaps();
        System.out.println(demoConfig.getListOfMaps().toString());

        Map<String, Map<String , String>> secondConfigs = secondDemoConfig.getListOfMaps();
        System.out.println(secondDemoConfig.getListOfMaps().toString());

        Map<String, ConnectionSpec> connections = rabbitConfigData.getConnections();
        Map<String, Map<String, QueueSpec>> queues = rabbitConfigData.getQueues();
        System.out.println(rabbitConfigData.getConnections().toString());
        System.out.println(rabbitConfigData.getQueues().toString());
    }
}
