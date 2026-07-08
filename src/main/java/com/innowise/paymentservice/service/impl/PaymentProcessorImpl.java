package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomClient;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentException;
import com.innowise.paymentservice.kafka.PaymentCompletedEvent;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessorImpl implements PaymentProcessor {
    private final PaymentRepository paymentRepository;
    private final RandomClient randomClient;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Override
    @Async
    public void processPayment(String id) {
        try {
            Thread.sleep(5000);

            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new PaymentException("Payment is not found!"));
            Integer number = randomClient.getInteger();

            if (number % 2 == 0) {
                payment.setStatus(PaymentStatus.SUCCESS);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);

            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    payment.getOrderId(),
                    payment.getStatus().name()
            );
            kafkaTemplate.send("payment-events", event.getOrderId().toString(), event);
        } catch (InterruptedException e) {
            throw new PaymentException("Payment processing error!", e);
        }
    }
}
