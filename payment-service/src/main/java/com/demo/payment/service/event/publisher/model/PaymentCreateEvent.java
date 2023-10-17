package com.demo.payment.service.event.publisher.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.demo.rabbitmq.outbox.service.model.MessageBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateEvent extends MessageBaseRequest {
    private String id;
    private String orderId;
    private Long customerId;
    private BigDecimal amount;
    private String source;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
