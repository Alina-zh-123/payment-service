package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.PaymentAmountDto;
import com.innowise.paymentservice.dto.PaymentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    PaymentDto createPayment(PaymentDto paymentDto);
    PaymentDto getPaymentById(String id);
    List<PaymentDto> getPaymentsWithFilter(Long userId, Long orderId, String status);
    PaymentAmountDto getPaymentAmountByUserId(Long userId, LocalDateTime begin, LocalDateTime end);
    PaymentAmountDto getAllPaymentAmount(LocalDateTime begin, LocalDateTime end);
}
