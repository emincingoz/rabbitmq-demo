package com.demo.payment.service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payment")
@Entity
public class Payment {

    @Id
    @UuidGenerator
    private String id;
    private String orderId;
    private Long customerId;
    private BigDecimal amount;
    private String source;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
