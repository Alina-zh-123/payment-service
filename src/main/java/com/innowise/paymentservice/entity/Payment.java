package com.innowise.paymentservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    @Field("order_id")
    private Long orderId;
    @Field("user_id")
    private Long userId;
    private PaymentStatus status;
    @Field("created_at")
    private LocalDateTime createdAt;
    @Field("payment_amount")
    private BigDecimal amount;
}