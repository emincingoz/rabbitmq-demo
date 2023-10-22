package com.demo.rabbitmq.outbox.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutboxDTO {
    private String uniqueId;
    private Long version;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String status;
    private String processCode;
    private String eventCode;
    private String header;
    private String payload;
}
