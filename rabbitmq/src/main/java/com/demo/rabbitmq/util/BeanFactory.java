package com.demo.rabbitmq.util;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public final class BeanFactory {
    private final DefaultListableBeanFactory factory;

    public BeanFactory(DefaultListableBeanFactory factory) {
        this.factory = factory;
    }

    public void initializeBean(Object bean, String beanName) {
        Object initialized = factory.initializeBean(bean, beanName);
        factory.autowireBeanProperties(initialized, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        factory.registerSingleton(beanName, initialized);
    }
}

