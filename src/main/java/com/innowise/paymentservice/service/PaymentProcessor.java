package com.innowise.paymentservice.service;

import org.springframework.stereotype.Service;

@Service
public interface PaymentProcessor {
    void processPayment(String id);
}
