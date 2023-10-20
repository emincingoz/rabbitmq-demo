package com.demo.rabbitmq.outbox.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message queue event class for publish and listen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueueEventDTO {
    /**
     * Outbox table unique id
     */
    private String outboxUniqueId;
}
