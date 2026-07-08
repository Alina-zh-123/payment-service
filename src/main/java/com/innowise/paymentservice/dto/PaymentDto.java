package com.innowise.paymentservice.dto;

import com.innowise.paymentservice.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDto {
    private String id;
    private Long orderId;
    private Long userId;

    @NotNull(message = "Status cannot be null")
    private PaymentStatus status;

    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDateTime createdAt;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal amount;
}
