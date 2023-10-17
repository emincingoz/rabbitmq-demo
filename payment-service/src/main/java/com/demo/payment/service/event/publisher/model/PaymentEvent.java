package com.demo.payment.service.event.publisher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentEvent<T> {
    /**
     * Data object for event
     */
    private T data;
}
