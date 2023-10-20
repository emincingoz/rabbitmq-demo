package com.demo.rabbitmq.outbox.repository.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class Outbox {
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
