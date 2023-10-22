package com.demo.rabbitmq.outbox.mapper;

import com.demo.rabbitmq.outbox.repository.entity.Outbox;
import com.demo.rabbitmq.outbox.model.OutboxDTO;

/**
 * Outbox dto entity mapper
 */
public interface OutboxMapper {

    /**
     * Map from entity to dto
     * @param outbox
     * @return
     */
    static OutboxDTO map2OutboxDTO(Outbox outbox) {
        if (outbox == null) {
            return null;
        }
        OutboxDTO outboxDTO = new OutboxDTO();
        outboxDTO.setUniqueId(outbox.getUniqueId());
        outboxDTO.setVersion(outbox.getVersion());
        outboxDTO.setCreatedDate(outbox.getCreatedDate());
        outboxDTO.setUpdatedDate(outbox.getUpdatedDate());
        outboxDTO.setStatus(outbox.getStatus());
        outboxDTO.setProcessCode(outbox.getProcessCode());
        outboxDTO.setEventCode(outbox.getEventCode());
        outboxDTO.setHeader(outbox.getHeader());
        outboxDTO.setPayload(outbox.getPayload());
        return outboxDTO;
    }

    /**
     * Map from dto to entity
     * @param outboxDTO
     * @return
     */
    static Outbox map2Outbox(OutboxDTO outboxDTO) {
        if (outboxDTO == null) {
            return null;
        }
        Outbox outbox = new Outbox();
        outbox.setUniqueId(outboxDTO.getUniqueId());
        outbox.setVersion(outboxDTO.getVersion());
        outbox.setCreatedDate(outboxDTO.getCreatedDate());
        outbox.setUpdatedDate(outboxDTO.getUpdatedDate());
        outbox.setStatus(outboxDTO.getStatus());
        outbox.setProcessCode(outboxDTO.getProcessCode());
        outbox.setEventCode(outboxDTO.getEventCode());
        outbox.setHeader(outboxDTO.getHeader());
        outbox.setPayload(outboxDTO.getPayload());
        return outbox;
    }
}
