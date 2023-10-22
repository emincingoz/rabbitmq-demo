package com.demo.rabbitmq.outbox.enums;

/**
 * Enum is used for all different jobs that will use the outbox pattern (it can be considered module-based).
 */
public enum ProcessType {
    ORDER,
    PAYMENT
}
