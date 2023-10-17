package com.demo.payment.service;

import java.util.Map;

import com.demo.payment.service.config.DemoConfig;
import com.demo.payment.service.config.SecondDemoConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;

@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
public class PaymentServiceApplication implements CommandLineRunner {

    private final DemoConfig demoConfig;
    private final SecondDemoConfig secondDemoConfig;

    public PaymentServiceApplication(DemoConfig demoConfig, SecondDemoConfig secondDemoConfig) {
        this.demoConfig = demoConfig;
        this.secondDemoConfig = secondDemoConfig;
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
    }
}
