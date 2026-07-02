package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.dto.PaymentAmountDto;
import com.innowise.paymentservice.entity.Payment;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    @Query("{ '$and': [ " +
            "?#{ [0] == null ? { $expr: true } : { 'user_id': [0] } }, " +
            "?#{ [1] == null ? { $expr: true } : { 'order_id': [1] } }, " +
            "?#{ [2] == null ? { $expr: true } : { 'status': [2] } } ] }")
    List<Payment> findPaymentBy(Long userId, Long orderId, String status);

    @Aggregation(pipeline = {
            "{ $match: { 'user_id' : ?0, 'created_at' : { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { _id: null, 'totalAmount': { $sum: '$payment_amount' } } }"
    })
    PaymentAmountDto findPaymentAmountByUserId(Long userId, LocalDateTime begin, LocalDateTime end);

    @Aggregation(pipeline = {
            "{ $match: { 'created_at' : { $gte: ?0, $lte: ?1 } } }",
            "{ $group: { _id: null, 'totalAmount': { $sum: '$payment_amount' } } }"
    })
    PaymentAmountDto findAllPaymentAmount(LocalDateTime begin, LocalDateTime end);
}
