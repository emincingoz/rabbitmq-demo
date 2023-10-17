package com.demo.payment.service.controller;

import com.demo.payment.service.service.PublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/payment")
public class PaymentConroller {
    private final PublisherService publisherService;

    public PaymentConroller(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping
    public ResponseEntity<?> sendMessage() {
        publisherService.sendMessage();
        return ResponseEntity.ok("OKAY");
    }
}
