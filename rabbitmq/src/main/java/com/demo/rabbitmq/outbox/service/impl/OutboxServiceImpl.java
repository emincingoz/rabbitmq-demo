package com.demo.rabbitmq.outbox.service.impl;

import java.util.Optional;

import com.demo.rabbitmq.outbox.mapper.OutboxMapper;
import com.demo.rabbitmq.outbox.repository.OutboxRepository;
import com.demo.rabbitmq.outbox.repository.entity.Outbox;
import com.demo.rabbitmq.outbox.service.OutboxService;
import com.demo.rabbitmq.outbox.service.enums.OutboxStatus;
import com.demo.rabbitmq.outbox.service.model.OutboxDTO;
import org.springframework.stereotype.Service;

/**
 * Outbox table service
 */
@Service
public class OutboxServiceImpl implements OutboxService {

    /**
     * Generic repository of outbox table
     */
    private final OutboxRepository outboxRepository;

    public OutboxServiceImpl(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    /**
     * Gets outbox dto by primary key
     * @param uniqueId
     * @return
     */
    @Override
    public OutboxDTO getById(String uniqueId) {
        Optional<Outbox> outbox = outboxRepository.getById(uniqueId);
        return OutboxMapper.map2OutboxDTO(outbox.orElse(null));
    }

    /**
     * Creates new outbox record
     * @param outboxDTO
     * @return
     */
    @Override
    public OutboxDTO create(OutboxDTO outboxDTO) {
        Outbox outbox = OutboxMapper.map2Outbox(outboxDTO);
        outboxRepository.insert(outbox);
        return outboxDTO;
    }

    /**
     * Updates status of outbox record
     * @param uniqueId
     * @param status
     */
    @Override
    public void updateStatus(String uniqueId, OutboxStatus status) {
        outboxRepository.updateStatus(uniqueId, status.name());
    }
}
