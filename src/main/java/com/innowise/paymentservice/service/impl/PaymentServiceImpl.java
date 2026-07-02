package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomClient;
import com.innowise.paymentservice.dto.PaymentAmountDto;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentException;
import com.innowise.paymentservice.kafka.PaymentCompletedEvent;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomClient randomClient;
    @Lazy
    private final PaymentServiceImpl self;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    private Jwt getPrincipal() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long currentUserId() {
        return Long.parseLong(getPrincipal().getClaimAsString("user_id"));
    }

    private boolean isAdmin() {
        String role = getPrincipal().getClaimAsString("role");
        return "ADMIN".equals(role);
    }

    @Async
    public void processPayment(String id) throws InterruptedException {
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
    }

    @Override
    public PaymentDto createPayment(PaymentDto paymentDto) throws InterruptedException {
        Payment payment = paymentMapper.dtoToPayment(paymentDto);

        payment.setUserId(currentUserId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        Payment res = paymentRepository.save(payment);

        self.processPayment(String.valueOf((res.getId())));

        return paymentMapper.paymentToDto(res);
    }

    @Override
    public PaymentDto getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment is not found!"));

        if (!isAdmin() && !payment.getUserId().equals(currentUserId())) {
            throw new AccessDeniedException("Access denied!");
        }

        return paymentMapper.paymentToDto(payment);
    }

    @Override
    public List<PaymentDto> getPaymentsWithFilter(Long userId, Long orderId, String status) {
        if (!isAdmin()) {
            userId = currentUserId();
        }

        return paymentRepository.findPaymentBy(userId, orderId, status).stream()
                .map(paymentMapper::paymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentAmountDto getPaymentAmountByUserId(Long userId, LocalDateTime begin, LocalDateTime end) {
        if (!isAdmin() && !userId.equals(currentUserId())) {
            throw new AccessDeniedException("Access denied!");
        }
        return paymentRepository.findPaymentAmountByUserId(userId, begin, end);
    }

    @Override
    public PaymentAmountDto getAllPaymentAmount(LocalDateTime begin, LocalDateTime end) {
        return paymentRepository.findAllPaymentAmount(begin, end);
    }
}
