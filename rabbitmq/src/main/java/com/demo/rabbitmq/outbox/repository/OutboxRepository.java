package com.demo.rabbitmq.outbox.repository;

import java.util.Optional;

import com.demo.rabbitmq.outbox.repository.entity.Outbox;

public interface OutboxRepository {
    Optional<Outbox> getById(String uniqueId);
    int insert(Outbox outbox);
    int update(Outbox outbox);
    int delete(String uniqueId);
    int updateStatus(String uniqueId, String status);
}
