package com.demo.rabbitmq.outbox.service;

import com.demo.rabbitmq.outbox.service.enums.OutboxStatus;
import com.demo.rabbitmq.outbox.service.model.OutboxDTO;

public interface OutboxService {
    OutboxDTO getById(String uniqueId);
    OutboxDTO create(OutboxDTO outboxDTO);
    void updateStatus(String uniqueId, OutboxStatus status);
}
