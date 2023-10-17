package com.demo.order.service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue(generator = "id-generator")
    @GenericGenerator(name = "id-generator", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    private Long customerId;
    private String productId;
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
