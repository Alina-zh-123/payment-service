package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto paymentToDto(Payment payment);
    Payment dtoToPayment(PaymentDto paymentDto);
}
