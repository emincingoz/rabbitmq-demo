package com.demo.payment.service.config;

import com.demo.payment.service.config.model.ExchangeSpec;
import com.demo.payment.service.config.model.QueueSpec;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

public final class EventUtils {

    public static Channel getChannel(ConnectionFactory connectionFactory) {
        Connection connection = connectionFactory.createConnection();
        Throwable var2 = null;

        Channel var3;
        try {
            var3 = connection.createChannel(true);
        } catch (Throwable var12) {
            var2 = var12;
            throw var12;
        } finally {
            if (connection != null) {
                if (var2 != null) {
                    try {
                        connection.close();
                    } catch (Throwable var11) {
                        var2.addSuppressed(var11);
                    }
                } else {
                    connection.close();
                }
            }
        }
        return var3;
    }

    public static boolean isValidExchangeType(QueueSpec queueSpec) {
        try {
            getExchange(queueSpec.getExchange());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static AbstractExchange getExchange(ExchangeSpec exchangeSpec) throws Exception {
        if ("direct".equals(exchangeSpec.getType())) {
            return new DirectExchange(exchangeSpec.getName());
        } else if ("topic".equals(exchangeSpec.getType())) {
            return new TopicExchange(exchangeSpec.getName());
        } else if ("headers".equals(exchangeSpec.getType())) {
            return new HeadersExchange(exchangeSpec.getName());
        } else if ("fanout".equals(exchangeSpec.getType())) {
            return new FanoutExchange(exchangeSpec.getName());
        } else {
            throw new Exception("Wrong Exchange Type");
        }
    }
}
